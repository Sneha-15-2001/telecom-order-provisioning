/**
 * Human-readable labels for backend status and enum values.
 *
 * The services speak in enum codes (PAYMENT_PENDING, INVENTORY_RESERVED). Those
 * are correct for a wire format and wrong for a screen: a retail or support
 * agent should read "Awaiting payment", not "PAYMENT_PENDING". Showing raw
 * codes is what makes an operator console look like an API explorer.
 *
 * One map, used everywhere, so the same status never reads two different ways
 * on two different pages.
 */

const ORDER_STATUS: Record<string, string> = {
  CREATED: 'Draft',
  VALIDATING: 'Checking eligibility',
  VALIDATED: 'Eligibility passed',
  PAYMENT_PENDING: 'Awaiting payment',
  PAYMENT_COMPLETED: 'Payment received',
  INVENTORY_RESERVED: 'Stock reserved',
  PROVISIONING: 'Provisioning',
  ACTIVATING: 'Activating service',
  COMPLETED: 'Active',
  FAILED: 'Failed',
  CANCELLED: 'Cancelled',
  ROLLING_BACK: 'Rolling back',
  RETRYING: 'Retrying',
};

const PROVISIONING_STATUS: Record<string, string> = {
  PENDING: 'Queued',
  IN_PROGRESS: 'In progress',
  COMPLETED: 'Activated',
  FAILED: 'Failed',
  ROLLED_BACK: 'Rolled back',
};

const RESERVATION_STATUS: Record<string, string> = {
  ACTIVE: 'Held',
  CONFIRMED: 'Confirmed',
  CANCELLED: 'Released',
  EXPIRED: 'Expired',
};

const NOTIFICATION_STATUS: Record<string, string> = {
  PENDING: 'Queued',
  SENT: 'Sent',
  FAILED: 'Failed',
  RETRYING: 'Retrying',
};

const RESOURCE_STATUS: Record<string, string> = {
  AVAILABLE: 'Available',
  RESERVED: 'Reserved',
  ALLOCATED: 'Allocated',
  QUARANTINED: 'Quarantined',
  EXPIRED: 'Expired',
};

const PAYMENT_STATUS: Record<string, string> = {
  PENDING: 'Pending',
  SUCCESS: 'Successful',
  FAILED: 'Failed',
  REFUNDED: 'Refunded',
};

const CUSTOMER_STATUS: Record<string, string> = {
  ACTIVE: 'Active',
  SUSPENDED: 'Suspended',
  BLOCKED: 'Blocked',
  CLOSED: 'Closed',
};

const ORDER_TYPE: Record<string, string> = {
  NEW_CONNECTION: 'New connection',
  UPGRADE: 'Plan upgrade',
  PLAN_CHANGE: 'Plan change',
  DEVICE_ONLY: 'Device only',
  BROADBAND: 'Broadband',
  BULK: 'Bulk order',
};

const ITEM_TYPE: Record<string, string> = {
  MOBILE_PLAN: 'Mobile plan',
  DEVICE: 'Handset / device',
  SIM: 'Physical SIM',
  ESIM: 'eSIM',
  BROADBAND: 'Broadband',
  ADDON: 'Add-on',
  ROAMING_PACK: 'Roaming pack',
  FIBER_EQUIPMENT: 'Fiber equipment',
};

const MAPS: Record<string, Record<string, string>> = {
  order: ORDER_STATUS,
  provisioning: PROVISIONING_STATUS,
  reservation: RESERVATION_STATUS,
  notification: NOTIFICATION_STATUS,
  resource: RESOURCE_STATUS,
  payment: PAYMENT_STATUS,
  customer: CUSTOMER_STATUS,
  orderType: ORDER_TYPE,
  itemType: ITEM_TYPE,
};

/** All maps, so a caller can fall back across every domain at once. */
function lookup(value: string): string | undefined {
  const upper = (value ?? '').toUpperCase();
  for (const map of Object.values(MAPS)) {
    if (map[upper]) return map[upper];
  }
  return undefined;
}

/**
 * Turn a code into words. Unknown values are returned unchanged rather than
 * blanked — a new backend status should still render something readable.
 */
export function humanize(code: string, domain?: keyof typeof MAPS): string {
  if (!code) return '';
  if (domain) {
    const hit = MAPS[domain]?.[code.toUpperCase()];
    if (hit) return hit;
  }
  const any = lookup(code);
  if (any) return any;
  // Unknown enum: split underscores so NEW_THING reads "New thing".
  return code.toLowerCase().replace(/_/g, ' ');
}

export const ORDER_STATUS_LABELS = ORDER_STATUS;
export const ORDER_TYPE_LABELS = ORDER_TYPE;
export const ITEM_TYPE_LABELS = ITEM_TYPE;