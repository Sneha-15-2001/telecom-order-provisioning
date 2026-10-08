package com.telecom.order.integration.dto;

/** Wire shape of inventory-service POST /api/inventory/reserve. */
public record InventoryReserveRequest(Long resourceId, Long orderId, Long customerId, Integer ttlMinutes) {}
