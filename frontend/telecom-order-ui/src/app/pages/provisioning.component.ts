import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ProvRequest, ProvisioningApiService } from '../services/provisioning-api.service';
import { StatusPillComponent } from '../shared/status-pill.component';
import { fromHttp } from '../shared/user-errors';

/** Provisioning console: simulated activation lifecycle (Phase 9). */
@Component({
  imports: [FormsModule, StatusPillComponent],
  selector: 'app-provisioning',
  templateUrl: './provisioning.component.html',
})
export class ProvisioningComponent implements OnInit {
  private api = inject(ProvisioningApiService);

  rows = signal<ProvRequest[]>([]);
  total = signal(0);
  status = signal('');
  error = signal('');
  msg = signal('');
  newOrderId = signal(0);
  newMsisdn = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.api.list(0, 20, this.status()).subscribe({
      next: (p) => {
        this.rows.set(p.content);
        this.total.set(p.totalElements);
      },
      error: (e) => this.error.set(fromHttp(e, 'provisioning', 'load')),
    });
  }

  step(r: ProvRequest, op: string): void {
    this.api.action(r.id, op).subscribe({
      next: () => {
        this.msg.set(`${r.requestNumber}: ${op} done.`);
        this.load();
      },
      error: (e) => this.msg.set(`Blocked: ${e.error?.message ?? op + ' failed'}`),
    });
  }

  quickMobile(): void {
    if (!this.newOrderId() || !this.newMsisdn()) {
      this.msg.set('Enter an order ID and MSISDN first.');
      return;
    }
    this.api
      .create({ orderId: this.newOrderId(), serviceType: 'MOBILE', msisdn: this.newMsisdn() })
      .subscribe({
        next: (r) => {
          this.msg.set(`${r.requestNumber} opened. Start + activate it below.`);
          this.load();
        },
        error: (e) => this.msg.set(`Open blocked: ${e.error?.message ?? 'failed'}`),
      });
  }
}
