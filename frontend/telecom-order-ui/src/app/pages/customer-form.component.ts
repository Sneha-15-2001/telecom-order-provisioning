import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CustomerApiService } from '../services/customer-api.service';
import { fromHttp } from '../shared/user-errors';

/** Customer onboarding form (Phase 9). */
@Component({
  imports: [FormsModule, RouterLink],
  selector: 'app-customer-form',
  templateUrl: './customer-form.component.html',
})
export class CustomerFormComponent {
  private api = inject(CustomerApiService);
  private router = inject(Router);

  model = { firstName: '', lastName: '', email: '', phone: '', customerType: 'INDIVIDUAL' };
  error = signal('');
  saving = signal(false);

  save(): void {
    this.saving.set(true);
    this.error.set('');
    this.api.create(this.model).subscribe({
      next: (c) => this.router.navigate(['/customers', c.id]),
      error: (e) => {
        this.error.set(fromHttp(e, 'customers', 'save'));
        this.saving.set(false);
      },
    });
  }
}
