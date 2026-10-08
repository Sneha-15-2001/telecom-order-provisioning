import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Notification, NotificationApiService } from '../services/notification-api.service';
import { StatusPillComponent } from '../shared/status-pill.component';

/** Notification console: queue, send, retry (Phase 9). */
@Component({
  imports: [FormsModule, StatusPillComponent],
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html',
})
export class NotificationListComponent implements OnInit {
  private api = inject(NotificationApiService);

  rows = signal<Notification[]>([]);
  total = signal(0);
  status = signal('');
  error = signal('');
  msg = signal('');
  quick = { orderId: 0, channel: 'SMS', recipient: '', event: 'ORDER_CREATED', orderNumber: '' };

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.list(0, 20, this.status()).subscribe({
      next: (p) => {
        this.rows.set(p.content);
        this.total.set(p.totalElements);
      },
      error: () => this.error.set('Could not reach notification-service (8085).'),
    });
  }

  send(n: Notification): void {
    this.api.send(n.id).subscribe({ next: () => { this.msg.set(`${n.notificationNumber} sent.`); this.load(); } });
  }

  retry(n: Notification): void {
    this.api.retry(n.id).subscribe({
      next: () => { this.msg.set(`${n.notificationNumber} retried.`); this.load(); },
      error: (e) => this.msg.set(`Retry blocked: ${e.error?.message ?? 'failed'}`),
    });
  }

  notify(): void {
    if (!this.quick.orderId || !this.quick.recipient) {
      this.msg.set('Enter an order ID and recipient first.');
      return;
    }
    this.api
      .notify({
        orderId: this.quick.orderId,
        channel: this.quick.channel,
        recipient: this.quick.recipient,
        event: this.quick.event,
        variables: { orderNumber: this.quick.orderNumber || String(this.quick.orderId) },
      })
      .subscribe({
        next: (n) => {
          this.msg.set(`${n.notificationNumber} queued. Send it below.`);
          this.load();
        },
        error: (e) => this.msg.set(`Queue blocked: ${e.error?.message ?? 'failed'}`),
      });
  }
}
