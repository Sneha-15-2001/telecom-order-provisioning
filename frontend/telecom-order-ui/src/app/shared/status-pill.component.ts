import { Component, Input } from '@angular/core';

const GREEN = new Set(['ACTIVE', 'COMPLETED', 'SENT', 'SUCCESS', 'VALIDATED', 'PAYMENT_COMPLETED', 'CONFIRMED', 'ALLOCATED']);
const AMBER = new Set(['PENDING', 'VALIDATING', 'PAYMENT_PENDING', 'IN_PROGRESS', 'RETRYING', 'RESERVED', 'RETRY']);
const RED = new Set(['FAILED', 'BLOCKED', 'SUSPENDED']);

/** Colored status pill used across all list/detail screens. */
@Component({
  selector: 'app-status',
  standalone: true,
  template: `<span class="pill {{ tone }}">{{ status }}</span>`,
})
export class StatusPillComponent {
  @Input({ required: true }) status!: string;

  get tone(): string {
    const s = (this.status ?? '').toUpperCase();
    if (GREEN.has(s)) return 'green';
    if (AMBER.has(s)) return 'amber';
    if (RED.has(s)) return 'red';
    if (s === 'CANCELLED' || s === 'ROLLED_BACK') return '';
    return 'navy';
  }
}
