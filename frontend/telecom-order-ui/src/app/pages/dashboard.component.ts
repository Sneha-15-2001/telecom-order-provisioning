import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CustomerApiService } from '../services/customer-api.service';
import { InventoryApiService } from '../services/inventory-api.service';
import { NotificationApiService } from '../services/notification-api.service';
import { OrderApiService } from '../services/order-api.service';
import { ProvisioningApiService } from '../services/provisioning-api.service';
import { CarouselComponent, Slide } from '../shared/carousel.component';
import { CountUpComponent } from '../shared/count-up.component';
import { RevealDirective } from '../shared/reveal.directive';
import { StatusPillComponent } from '../shared/status-pill.component';
import { fromHttp } from '../shared/user-errors';

interface Health {
  name: string;
  port: number;
  up: boolean;
}

/** NexaTel home: live KPIs, service health, attention queues. Phase 9. */
@Component({
  imports: [RouterLink, StatusPillComponent, CarouselComponent, CountUpComponent, RevealDirective],
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private customers = inject(CustomerApiService);
  private orders = inject(OrderApiService);
  private inventory = inject(InventoryApiService);
  private provisioning = inject(ProvisioningApiService);
  private notifications = inject(NotificationApiService);

  loading = signal(true);
  error = signal('');
  stats = signal({ customers: 0, orders: 0, failedOrders: 0, pendingPayments: 0, failedProv: 0, failedNotif: 0, freeMsisdn: 0 });
  health = signal<Health[]>([]);
  failedOrders = signal<{ id: number; orderNumber: string; status: string }[]>([]);

  slides: Slide[] = [
    {
      eyebrow: 'NexaTel 5G Plus',
      title: 'Recharge, pay bills & manage everything',
      sub: 'One storefront for mobile, broadband, orders, support and services.',
      cta: 'Recharge / New order',
      link: '/orders/new',
      cta2: 'Track order',
      link2: '/orders',
      art: 'burst',
    },
    {
      eyebrow: 'NexaTel Xstream Fiber',
      title: 'Blazing fiber, activated in minutes',
      sub: 'Reserve a fiber port, provision broadband and notify the customer — one saga.',
      cta: 'Provision broadband',
      link: '/provisioning',
      cta2: 'Check inventory',
      link2: '/inventory',
      art: 'waves',
    },
    {
      eyebrow: 'NexaTel Business',
      title: 'Clean queues, zero stuck orders',
      sub: 'Triage failures, retry provisioning and re-send notifications from one hub.',
      cta: 'Open triage',
      link: '/orders',
      cta2: 'View notifications',
      link2: '/notifications',
      art: 'rings',
    },
  ];

  ngOnInit(): void {
    forkJoin({
      customers: this.customers.list(0, 1),
      orders: this.orders.list(0, 1),
      failed: this.orders.list(0, 5, 'FAILED'),
      pending: this.orders.list(0, 1, 'PAYMENT_PENDING'),
      failedProv: this.provisioning.list(0, 1, 'FAILED'),
      failedNotif: this.notifications.list(0, 1, 'FAILED'),
      msisdn: this.inventory.available('MSISDN', 100),
    }).subscribe({
      next: (r) => {
        this.stats.set({
          customers: r.customers.totalElements,
          orders: r.orders.totalElements,
          failedOrders: r.failed.totalElements,
          pendingPayments: r.pending.totalElements,
          failedProv: r.failedProv.totalElements,
          failedNotif: r.failedNotif.totalElements,
          freeMsisdn: r.msisdn.length,
        });
        this.failedOrders.set(r.failed.content.map((o) => ({ id: o.id, orderNumber: o.orderNumber, status: o.status })));
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(fromHttp(e, 'orders', 'load'));
        this.loading.set(false);
      },
    });

    const services = [
      { name: 'customer', port: 8081 },
      { name: 'order', port: 8082 },
      { name: 'inventory', port: 8083 },
      { name: 'provisioning', port: 8084 },
      { name: 'notification', port: 8085 },
    ];
    services.forEach((s) => {
      // NOTE: /api/system/ping (not /actuator/health) — backends allow CORS for
      // /api/** only, so actuator calls from the browser are blocked. Both return UP.
      fetch(`http://localhost:${s.port}/api/system/ping`)
        .then((r) => r.json())
        .then((j) => this.health.update((h) => [...h, { ...s, up: j.status === 'UP' }]))
        .catch(() => this.health.update((h) => [...h, { ...s, up: false }]));
    });
  }
}
