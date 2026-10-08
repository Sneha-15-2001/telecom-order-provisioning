package com.telecom.order.entity;

/** Full order lifecycle. Downstream steps (inventory/provisioning) wire in via Phase 7/10. */
public enum OrderStatus {
  CREATED,
  VALIDATING,
  VALIDATED,
  PAYMENT_PENDING,
  PAYMENT_COMPLETED,
  INVENTORY_RESERVED,
  PROVISIONING,
  ACTIVATING,
  COMPLETED,
  FAILED,
  CANCELLED,
  ROLLING_BACK,
  RETRYING
}
