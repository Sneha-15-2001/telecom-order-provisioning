/**
 * Which lifecycle actions are legal from each order status.
 *
 * The order detail screen used to render every button at all times, so an agent
 * could click Submit on a CANCELLED order and find out afterwards, from an
 * error message, that it was not allowed. That is not how a real order console
 * behaves: the UI knows the lifecycle and offers only what is actually possible
 * from here.
 *
 * This mirrors the guards in order-service (OrderService.transition rules). The
 * backend remains the authority — this is a usability layer, not a second source
 * of truth. Anything not listed is not offered.
 */

export type OrderActionId =
  | 'validate' | 'submit' | 'pay' | 'promo' | 'fulfill'
  | 'retry' | 'reprocess' | 'cancel' | 'rollback';

export interface OrderAction {
  id: OrderActionId;
  /** Verbatim operation segment used by OrderApiService.action(). */
  op: string;
  label: string;
  /** Short explanation of what this does and why it is available now. */
  hint: string;
  /** Irreversible or externally visible — requires explicit confirmation. */
  destructive?: boolean;
  /** Only offered on the order-detail screen, not in the main row. */
  primary?: boolean;
}

const ACTIONS: Record<OrderActionId, OrderAction> = {
  validate: {
    id: 'validate', op: 'validate', label: 'Check eligibility',
    hint: 'Confirms the customer may buy this before anything is reserved.',
  },
  submit: {
    id: 'submit', op: 'submit', label: 'Send for payment',
    hint: 'Locks the basket and makes the amount payable.',
    primary: true,
  },
  pay: {
    id: 'pay', op: '', label: 'Take payment',
    hint: 'Records the payment and validates it against the amount due.',
    primary: true,
  },
  promo: {
    id: 'promo', op: 'promotion/apply', label: 'Apply promotion',
    hint: 'Applies a promotion code and recalculates the amount due.',
  },
  fulfill: {
    id: 'fulfill', op: 'fulfill', label: 'Activate',
    hint: 'Reserves stock, activates the service and notifies the customer.',
    primary: true,
  },
  retry: {
    id: 'retry', op: 'retry', label: 'Retry',
    hint: 'Moves a failed order back for another attempt without changing it.',
  },
  reprocess: {
    id: 'reprocess', op: 'reprocess', label: 'Start over',
    hint: 'Returns the order to draft so it can be checked and sent again.',
    destructive: true,
  },
  cancel: {
    id: 'cancel', op: 'cancel', label: 'Cancel order',
    hint: 'Cancels the order and releases anything reserved for it.',
    destructive: true,
  },
  rollback: {
    id: 'rollback', op: 'rollback', label: 'Reverse activation',
    hint: 'Compensates a provisioned order: releases stock and deactivates.',
    destructive: true,
  },
};

/**
 * Legal transitions per status, matching OrderService's guards.
 *
 * CANCELLED and COMPLETED are terminal — nothing is offered from them, which is
 * what a real console does rather than showing buttons that will bounce.
 */
const ALLOWED: Record<string, OrderActionId[]> = {
  CREATED: ['validate', 'submit', 'cancel'],
  RETRYING: ['validate', 'submit', 'cancel'],
  VALIDATING: ['cancel'],
  VALIDATED: ['submit', 'promo', 'cancel'],
  PAYMENT_PENDING: ['pay', 'promo', 'cancel'],
  PAYMENT_COMPLETED: ['fulfill', 'cancel', 'rollback'],
  INVENTORY_RESERVED: ['fulfill', 'cancel', 'rollback'],
  PROVISIONING: ['fulfill', 'rollback'],
  ACTIVATING: ['fulfill', 'rollback'],
  FAILED: ['retry', 'reprocess', 'cancel'],
  ROLLING_BACK: [],
  COMPLETED: ['rollback'],
  CANCELLED: [],
};

export function actionsFor(status: string): OrderAction[] {
  const ids = ALLOWED[(status ?? '').toUpperCase()] ?? [];
  return ids.map((id) => ACTIONS[id]);
}

export function isTerminal(status: string): boolean {
  return (ALLOWED[(status ?? '').toUpperCase()] ?? []).length === 0;
}

/** One line telling the user why there is nothing to click. */
export function terminalMessage(status: string): string {
  const s = (status ?? '').toUpperCase();
  if (s === 'COMPLETED') {
    return 'This order is complete. Nothing further is required — the service is active.';
  }
  if (s === 'CANCELLED') {
    return 'This order was cancelled and cannot be resumed. Create a new order if the customer still wants this service.';
  }
  if (s === 'ROLLING_BACK') {
    return 'A reversal is in progress. The order will settle shortly.';
  }
  return 'No actions are available from this state.';
}