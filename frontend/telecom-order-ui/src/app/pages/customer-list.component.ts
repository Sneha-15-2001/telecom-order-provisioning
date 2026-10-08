import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Customer, CustomerApiService } from '../services/customer-api.service';
import { StatusPillComponent } from '../shared/status-pill.component';

/** Customer list with search + status filter (Phase 9). */
@Component({
  imports: [RouterLink, FormsModule, StatusPillComponent],
  selector: 'app-customer-list',
  templateUrl: './customer-list.component.html',
})
export class CustomerListComponent implements OnInit {
  private api = inject(CustomerApiService);

  rows = signal<Customer[]>([]);
  total = signal(0);
  q = signal('');
  status = signal('');
  loading = signal(true);
  error = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    const call = this.q()
      ? this.api.search(this.q(), 0, 20)
      : this.api.list(0, 20, this.status());
    call.subscribe({
      next: (p) => {
        this.rows.set(p.content);
        this.total.set(p.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not reach customer-service (8081).');
        this.loading.set(false);
      },
    });
  }
}
