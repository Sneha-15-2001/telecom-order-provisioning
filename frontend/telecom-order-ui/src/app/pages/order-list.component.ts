import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Order, OrderApiService } from '../services/order-api.service';
import { StatusPillComponent } from '../shared/status-pill.component';
import { fromHttp } from '../shared/user-errors';

/** Order queue with status filter (Phase 9). */
@Component({
  imports: [RouterLink, FormsModule, StatusPillComponent],
  selector: 'app-order-list',
  templateUrl: './order-list.component.html',
})
export class OrderListComponent implements OnInit {
  private api = inject(OrderApiService);

  rows = signal<Order[]>([]);
  total = signal(0);
  status = signal('');
  loading = signal(true);
  error = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.list(0, 20, this.status()).subscribe({
      next: (p) => {
        this.rows.set(p.content);
        this.total.set(p.totalElements);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(fromHttp(e, 'orders', 'load'));
        this.loading.set(false);
      },
    });
  }
}
