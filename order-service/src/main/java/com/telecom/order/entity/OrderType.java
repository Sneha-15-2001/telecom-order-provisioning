package com.telecom.order.entity;

/**
 * Kind of order.
 *
 * <p>Each type carries the line item it must contain, so validation can reject a
 * broadband order with only a handset on it. Previously this enum was written to
 * the row and read back into the response without ever changing behaviour — six
 * values that all behaved identically.
 */
public enum OrderType {
  /** Selling a service to someone who does not have one. */
  NEW_CONNECTION(OrderItemType.MOBILE_PLAN),
  /** Moving an existing subscriber onto a better plan. */
  UPGRADE(OrderItemType.MOBILE_PLAN),
  /** Switching an existing subscriber to a different plan. */
  PLAN_CHANGE(OrderItemType.MOBILE_PLAN),
  /** Hardware only — no plan, no service change. */
  DEVICE_ONLY(OrderItemType.DEVICE),
  /** Fixed-line broadband; requires an address that can be served. */
  BROADBAND(OrderItemType.BROADBAND),
  /** Many lines under one account; created through the bulk endpoint. */
  BULK(OrderItemType.MOBILE_PLAN);

  private final OrderItemType requiredItemType;

  OrderType(OrderItemType requiredItemType) {
    this.requiredItemType = requiredItemType;
  }

  /** The line item this order type is meaningless without. */
  public OrderItemType getRequiredItemType() {
    return requiredItemType;
  }
}