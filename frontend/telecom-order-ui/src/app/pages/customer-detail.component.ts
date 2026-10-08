import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Customer, CustomerApiService } from '../services/customer-api.service';
import { StatusPillComponent } from '../shared/status-pill.component';

/** Customer 360°: profile, eligibility/validation, subscriptions, history (Phase 9). */
@Component({
  imports: [RouterLink, StatusPillComponent],
  selector: 'app-customer-detail',
  templateUrl: './customer-detail.component.html',
})
export class CustomerDetailComponent implements OnInit {
  private api = inject(CustomerApiService);
  private route = inject(ActivatedRoute);

  customer = signal<Customer | null>(null);
  eligibility = signal<{ eligible: boolean; reasons: string[] } | null>(null);
  validation = signal<{ valid: boolean; status: string; reasons: string[] } | null>(null);
  history = signal<{ type: string; description: string; timestamp: string }[]>([]);
  subs = signal<unknown[]>([]);
  error = signal('');
  msg = signal('');

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.api.get(id).subscribe({ next: (c) => this.customer.set(c), error: () => this.error.set('Customer not found or service down.') });
    this.api.eligibility(id).subscribe({ next: (e) => this.eligibility.set(e) });
    this.api.history(id).subscribe({ next: (h) => this.history.set(h.events) });
    this.api.subscriptions(id).subscribe({ next: (s) => this.subs.set(s) });
  }

  id(): number {
    return this.customer()?.id ?? 0;
  }

  suspend(): void {
    this.api.suspend(this.id()).subscribe({ next: (c) => { this.customer.set(c); this.msg.set('Customer suspended.'); } });
  }

  reactivate(): void {
    this.api.reactivate(this.id()).subscribe({ next: (c) => { this.customer.set(c); this.msg.set('Customer reactivated.'); } });
  }

  validate(): void {
    this.api.validate(this.id()).subscribe({ next: (v) => this.validation.set(v) });
  }
}
