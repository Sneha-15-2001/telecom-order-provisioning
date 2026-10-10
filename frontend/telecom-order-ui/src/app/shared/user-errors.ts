/**
 * User-facing error messages.
 *
 * The screens previously reported failures with the developer in mind:
 * "Create failed — is customer-service (8081) up?" and "Backend unreachable —
 * start the five services (8081–8085) first." Port numbers are our wiring, not
 * the user's problem — someone using an order console does not know or care
 * which microservice is on 8081.
 *
 * `fromHttp` turns a transport failure into something an operator can act on,
 * and keeps the technical detail available for a support engineer rather than
 * dumping it on screen.
 */
import { HttpErrorResponse } from '@angular/common/http';

/** What the user is trying to do, used to phrase the message. */
export type Area = 'customers' | 'orders' | 'inventory' | 'provisioning' | 'notifications' | 'catalogue';

const SUBJECT: Record<Area, string> = {
  customers: 'customers',
  orders: 'orders',
  inventory: 'stock',
  provisioning: 'activation',
  notifications: 'notifications',
  catalogue: 'the product catalogue',
};

/**
 * A message safe to show an operator.
 *
 * A 4xx from our own API carries a deliberate message worth showing (validation
 * failed, order already cancelled). A 5xx or a transport failure does not — the
 * user cannot act on a stack trace, so we say what broke and offer a retry.
 */
export function fromHttp(err: unknown, area: Area, action = 'load'): string {
  if (!(err instanceof HttpErrorResponse)) {
    return `Could not ${action} ${SUBJECT[area]}. Try again.`;
  }

  if (err.status === 0) {
    return `Lost connection while trying to ${action} ${SUBJECT[area]}. Check your connection and try again.`;
  }

  // Our API returns {message} for deliberate rejections; those are actionable.
  const detail = err.error?.message ?? err.error?.detail;
  if (detail && typeof detail === 'string' && err.status < 500) {
    return detail;
  }

  if (err.status === 404) {
    return `That ${SUBJECT[area].replace(/s$/, '')} could not be found. It may have been removed.`;
  }
  if (err.status === 409) {
    return `That ${SUBJECT[area].replace(/s$/, '')} was changed by someone else. Refresh and try again.`;
  }
  if (err.status >= 500) {
    return `Something went wrong on our side while trying to ${action} ${SUBJECT[area]}. Try again in a moment.`;
  }
  return `Could not ${action} ${SUBJECT[area]}. Try again.`;
}