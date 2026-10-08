package com.telecom.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Per-item result of a bulk order request")
public record BulkOrderItemResult(
    boolean success, String orderNumber, Long orderId, String error) {}
