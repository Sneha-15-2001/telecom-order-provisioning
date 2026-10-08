package com.telecom.order.dto;

import com.telecom.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "One immutable order history entry")
public record OrderHistoryEntry(
    Long id, String event, OrderStatus fromStatus, OrderStatus toStatus,
    String comment, LocalDateTime timestamp) {}
