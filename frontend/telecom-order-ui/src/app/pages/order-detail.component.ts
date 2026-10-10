import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Order, OrderApiService } from '../services/order-api.service';
import { StatusPillComponent } from '../shared/status-pill.component';
import { actionsFor, isTerminal, terminalMessage, type OrderAction } from '../shared/order-actions';
import { humanize } from '../shared/status-labels';
import { API_ENDPOINTS } from '../core/api-endpoints';

type Notice = { kind: 'ok' | 'error'; text: string };

/** Order cockpit: lifecycle actions, payment, promo, timeline. */
@Component({
  imports: [RouterLink, FormsModule, StatusPillComponent],
  selector: 'app-order-detail',
  templateUrl: './order-detail.component.html',
})
export class OrderDetailComponent implements OnInit {
  private api = inject(OrderApiService);
  private http = inject(HttpClient);
  private route = inject(ActivatedRoute);

  order = signal<Order | null>(null);
  timeline = signal<{ timestamp: string; title: string; detail: string }[]>([]);
  /** Success and failure get separate channels — they used to share one
   *  green-tinted banner, which styled a blocked action as a success. */
  notice = signal<Notice | null>(null);
  confirm = signal<OrderAction | null>(null);
  payAmount = signal(0);
  payMethod = signal('UPI');
  promoCode = signal('');
  busy = signal(false);
  fulfillment = signal<{ orderStatus: string; steps: { step: string; status: string; detail: string }[] } | null>(null);

  /** Only the transitions legal from the current status. */
  actions = computed(() => actionsFor(this.order()?.status ?? ''));
  primaryActions = computed(() => this.actions().filter((a) => a.primary));
  secondaryActions = computed(() => this.actions().filter((a) => !a.primary));
  terminal = computed(() => isTerminal(this.order()?.status ?? ''));
  terminalNote = computed(() => terminalMessage(this.order()?.status ?? ''));
  overpay = computed(() => this.payAmount() > (this.order()?.payableAmount ?? 0));

  /** Exposed to the template for enum labels. */
  humanize = (code: string, domain?: 'order' | 'orderType' | 'itemType') => humanize(code, domain);

  ngOnInit(): void {
    this.reload();
  }

  id(): number {
    return Number(this.route.snapshot.paramMap.get('id'));
  }

  reload(): void {
    this.api.get(this.id()).subscribe({
      next: (o) => {
        this.order.set(o);
        this.payAmount.set(o.payableAmount);
      },
      error: () => this.notice.set({ kind: 'error', text: 'Order not found. It may have been removed.' }),
    });
    this.api.timeline(this.id()).subscribe({ next: (t) => this.timeline.set(t.events) });
  }

  /**
   * Destructive actions are gated behind a confirmation panel. The previous
   * version fired Cancel, Rollback and Start-over on a single click.
   */
  invoke(action: OrderAction): void {
    if (action.destructive) {
      this.confirm.set(action);
      return;
    }
    if (action.id === 'pay') {
      this.pay();
      return;
    }
    if (action.id === 'promo') {
      this.applyPromo();
      return;
    }
    if (action.id === 'fulfill') {
      // Goes through the saga endpoint rather than the generic action call,
      // because it returns per-step detail worth showing.
      this.fulfill();
      return;
    }
    this.run(action);
  }

  cancelConfirm(): void {
    this.confirm.set(null);
  }

  confirmAction(): void {
    const a = this.confirm();
    if (!a) return;
    this.confirm.set(null);
    this.run(a);
  }

  private run(action: OrderAction): void {
    this.busy.set(true);
    this.notice.set(null);
    this.api.action(this.id(), action.op).subscribe({
      next: (o) => {
        this.order.set(o);
        this.busy.set(false);
        this.notice.set({ kind: 'ok', text: `${action.label} complete — order is now ${humanize(o.status, 'order')}.` });
        this.reload();
      },
      error: (e) => {
        this.busy.set(false);
        this.notice.set({ kind: 'error', text: e.error?.message ?? `${action.label} could not be completed.` });
      },
    });
  }

  pay(): void {
    this.busy.set(true);
    this.notice.set(null);
    this.api.recordPayment(this.id(), this.payAmount(), this.payMethod()).subscribe({
      next: () => {
        this.api.validatePayment(this.id()).subscribe({
          next: (r) => {
            this.busy.set(false);
            this.notice.set({
              kind: r.valid ? 'ok' : 'error',
              text: r.valid
                ? 'Payment received and validated.'
                : 'Payment was recorded but did not cover the amount due.',
            });
            this.reload();
          },
          error: (e) => {
            this.busy.set(false);
            this.notice.set({ kind: 'error', text: e.error?.message ?? 'Payment could not be validated.' });
          },
        });
      },
      error: (e) => {
        this.busy.set(false);
        this.notice.set({ kind: 'error', text: e.error?.message ?? 'Payment could not be recorded.' });
      },
    });
  }

  applyPromo(): void {
    const code = this.promoCode().trim();
    if (!code) {
      this.notice.set({ kind: 'error', text: 'Enter a promotion code first.' });
      return;
    }
    this.busy.set(true);
    this.notice.set(null);
    this.api.applyPromo(this.id(), code).subscribe({
      next: (o) => {
        this.order.set(o);
        this.payAmount.set(o.payableAmount);
        this.busy.set(false);
        this.promoCode.set('');
        this.notice.set({ kind: 'ok', text: 'Promotion applied — the amount due has changed.' });
      },
      error: (e) => {
        this.busy.set(false);
        this.notice.set({ kind: 'error', text: e.error?.message ?? 'Promotion could not be applied.' });
      },
    });
  }

  /**
   * Activation runs the inventory -> provisioning -> notification saga.
   * The fault-injection hook (`failAt`) is deliberately not wired to the UI:
   * it is a test control and belongs in the backend's dev profile, not on a
   * screen an agent operates from.
   */
  fulfill(): void {
    this.busy.set(true);
    this.notice.set(null);
    const url = `${API_ENDPOINTS.order}/api/orders/${this.id()}/fulfill`;
    this.http.post<{ orderStatus: string; steps: { step: string; status: string; detail: string }[] }>(url, {}).subscribe({
      next: (f) => {
        this.fulfillment.set(f);
        this.busy.set(false);
        this.notice.set({ kind: 'ok', text: `Activation finished — order is ${humanize(f.orderStatus, 'order')}.` });
        this.reload();
      },
      error: (e) => {
        this.busy.set(false);
        this.notice.set({ kind: 'error', text: e.error?.message ?? 'Activation could not be completed.' });
      },
    });
  }
}