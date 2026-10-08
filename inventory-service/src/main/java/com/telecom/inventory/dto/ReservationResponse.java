package com.telecom.inventory.dto;

import com.telecom.inventory.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "A resource reservation for an order")
public record ReservationResponse(
    Long id,
    String reservationNumber,
    Long resourceId,
    String resourceNumber,
    Long orderId,
    Long customerId,
    ReservationStatus status,
    LocalDateTime reservedAt,
    LocalDateTime expiresAt,
    LocalDateTime confirmedAt) {}
