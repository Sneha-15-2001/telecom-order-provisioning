import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { OrderApiService } from '../services/order-api.service';

/** Guided order creation: customer + one line item (Phase 9). */
@Component({
  imports: [FormsModule, RouterLink],
  selector: 'app-order-form',
  templateUrl: './order-form.component.html',
})
export class OrderFormComponent {
  private api = inject(OrderApiService);
  private router = inject(Router);

  model = {
    customerId: 1,
    customerNumber: 'CUS-DEMO001',
    orderType: 'NEW_CONNECTION',
    items: [{ itemType: 'MOBILE_PLAN', productCode: 'PLAN_5G_299', productName: '5G Unlimited 299', quantity: 1, unitPrice: 299 }],
  };
  error = signal('');
  saving = signal(false);

  save(): void {
    this.saving.set(true);
    this.error.set('');
    this.api.create(this.model).subscribe({
      next: (o) => this.router.navigate(['/orders', o.id]),
      error: (e) => {
        this.error.set(e.error?.message ?? 'Create failed — is order-service (8082) up?');
        this.saving.set(false);
      },
    });
  }
}
