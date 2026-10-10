import { Component, Input } from '@angular/core';
import { humanize } from './status-labels';

const GREEN = new Set(['ACTIVE', 'COMPLETED', 'SENT', 'SUCCESS', 'VALIDATED', 'PAYMENT_COMPLETED', 'CONFIRMED', 'ALLOCATED', 'AVAILABLE']);
const AMBER = new Set(['PENDING', 'VALIDATING', 'PAYMENT_PENDING', 'IN_PROGRESS', 'RETRYING', 'RESERVED', 'RETRY', 'INVENTORY_RESERVED', 'PROVISIONING', 'ACTIVATING', 'CREATED']);
const RED = new Set(['FAILED', 'BLOCKED', 'SUSPENDED', 'EXPIRED', 'QUARANTINED']);

/**
 * Status pill used across every list and detail screen.
 *
 * Renders readable words rather than the wire enum. A customer-care agent should
 * read "Awaiting payment", not PAYMENT_PENDING — showing raw codes is what made
 * this look like an API explorer instead of an order console. The underlying
 * code is still available as a tooltip for anyone diagnosing a problem.
 */
@Component({
  selector: 'app-status',
  standalone: true,
  template: `<span class="pill {{ tone }}" [title]="status">{{ label }}</span>`,
})
export class StatusPillComponent {
  @Input({ required: true }) status!: string;
  /** Optional domain hint, e.g. 'order' — improves label accuracy. */
  @Input() domain?: 'order' | 'provisioning' | 'reservation' | 'notification' | 'resource' | 'payment' | 'customer';

  get label(): string {
    return humanize(this.status, this.domain);
  }

  get tone(): string {
    const s = (this.status ?? '').toUpperCase();
    if (GREEN.has(s)) return 'green';
    if (AMBER.has(s)) return 'amber';
    if (RED.has(s)) return 'red';
    if (s === 'CANCELLED' || s === 'ROLLED_BACK') return '';
    return 'navy';
  }
}