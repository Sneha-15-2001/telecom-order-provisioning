package com.telecom.order.entity;

/** Kind of order (drives future orchestration branching in Phase 10). */
public enum OrderType {
  NEW_CONNECTION,
  UPGRADE,
  PLAN_CHANGE,
  DEVICE_ONLY,
  BROADBAND,
  BULK
}
