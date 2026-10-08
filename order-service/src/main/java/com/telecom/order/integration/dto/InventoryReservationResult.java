package com.telecom.order.integration.dto;

/** Wire shape of inventory-service reservation responses (status as String). */
public record InventoryReservationResult(
    Long id, String reservationNumber, Long resourceId, String resourceNumber,
    Long orderId, Long customerId, String status) {}
