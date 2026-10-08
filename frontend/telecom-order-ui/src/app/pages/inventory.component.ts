import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { InvResource, InventoryApiService, Reservation } from '../services/inventory-api.service';
import { StatusPillComponent } from '../shared/status-pill.component';

/** Inventory console: stock, availability, reserve → confirm → release (Phase 9). */
@Component({
  imports: [FormsModule, StatusPillComponent],
  selector: 'app-inventory',
  templateUrl: './inventory.component.html',
})
export class InventoryComponent implements OnInit {
  private api = inject(InventoryApiService);

  rows = signal<InvResource[]>([]);
  total = signal(0);
  type = signal('');
  status = signal('');
  holds = signal<Reservation[]>([]);
  error = signal('');
  msg = signal('');
  reserveOrderId = signal(0);
  quickResId = signal(0);

  ngOnInit(): void {
    this.load();
    this.loadHolds();
  }

  load(): void {
    this.api.list(0, 20, this.type(), this.status()).subscribe({
      next: (p) => {
        this.rows.set(p.content);
        this.total.set(p.totalElements);
      },
      error: () => this.error.set('Could not reach inventory-service (8083).'),
    });
  }

  loadHolds(): void {
    this.api.reservations('', 0, 10).subscribe({ next: (p) => this.holds.set(p.content) });
  }

  reserve(r: InvResource): void {
    if (!this.reserveOrderId()) {
      this.msg.set('Enter an order ID first.');
      return;
    }
    this.api.reserve(r.id, this.reserveOrderId()).subscribe({
      next: (s) => {
        this.msg.set(`Reserved ${s.reservationNumber} for order ${s.orderId}. Confirm it below.`);
        this.load();
        this.loadHolds();
      },
      error: (e) => this.msg.set(`Reserve blocked: ${e.error?.message ?? 'failed'}`),
    });
  }

  confirm(s: Reservation): void {
    this.api.confirm(s.id).subscribe({ next: () => { this.msg.set(`${s.reservationNumber} confirmed (ALLOCATED).`); this.load(); this.loadHolds(); } });
  }

  release(s: Reservation): void {
    this.api.cancel(s.id).subscribe({ next: () => { this.msg.set(`${s.reservationNumber} released.`); this.load(); this.loadHolds(); } });
  }

  /** Direct confirm/release by reservation ID — same as POST /reservations/{id}/confirm|cancel. */
  confirmById(): void {
    if (!this.quickResId()) {
      this.msg.set('Enter a reservation ID first.');
      return;
    }
    this.api.confirm(this.quickResId()).subscribe({
      next: (s) => {
        this.msg.set(`${s.reservationNumber} confirmed (ALLOCATED).`);
        this.load();
        this.loadHolds();
      },
      error: (e) => this.msg.set(`Confirm blocked: ${e.error?.message ?? 'failed'}`),
    });
  }

  releaseById(): void {
    if (!this.quickResId()) {
      this.msg.set('Enter a reservation ID first.');
      return;
    }
    this.api.cancel(this.quickResId()).subscribe({
      next: (s) => {
        this.msg.set(`${s.reservationNumber} released.`);
        this.load();
        this.loadHolds();
      },
      error: (e) => this.msg.set(`Release blocked: ${e.error?.message ?? 'failed'}`),
    });
  }
}
