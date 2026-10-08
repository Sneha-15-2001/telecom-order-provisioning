import { Component, OnInit, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Order, OrderApiService } from '../services/order-api.service';
import { StatusPillComponent } from '../shared/status-pill.component';

/** Order cockpit: lifecycle actions, payment, promo, timeline (Phase 9). */
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
  error = signal('');
  msg = signal('');
  payAmount = signal(0);
  payMethod = signal('UPI');
  promoCode = signal('FESTIVE10');
  fulfillment = signal<{ orderStatus: string; steps: { step: string; status: string; detail: string }[] } | null>(null);

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
      error: () => this.error.set('Order not found or service down.'),
    });
    this.api.timeline(this.id()).subscribe({ next: (t) => this.timeline.set(t.events) });
  }

  run(op: string, body: unknown = {}): void {
    this.msg.set('');
    this.api.action(this.id(), op, body).subscribe({
      next: (o) => {
        this.order.set(o);
        this.payAmount.set(o.payableAmount);
        this.msg.set(`Done: ${op} → ${o.status}`);
        this.reload();
      },
      error: (e) => this.msg.set(`Blocked: ${e.error?.message ?? op + ' failed'}`),
    });
  }

  pay(): void {
    this.api.recordPayment(this.id(), this.payAmount(), this.payMethod()).subscribe({
      next: () => this.run('payment/validate'),
      error: (e) => this.msg.set(`Payment blocked: ${e.error?.message ?? 'failed'}`),
    });
  }

  applyPromo(): void {
    this.run('promotion/apply', { promoCode: this.promoCode() });
  }

  fulfill(failAt = ''): void {
    this.msg.set('Fulfilling across inventory → provisioning → notification…');
    const url = `http://localhost:8082/api/orders/${this.id()}/fulfill${failAt ? `?failAt=${failAt}` : ''}`;
    this.http.post<{ orderStatus: string; steps: { step: string; status: string; detail: string }[] }>(url, {}).subscribe({
      next: (f) => {
        this.fulfillment.set(f);
        this.msg.set(`Fulfillment finished: ${f.orderStatus}`);
        this.reload();
      },
      error: (e) => this.msg.set(`Fulfill blocked: ${e.error?.message ?? 'failed'}`),
    });
  }
}
