package com.telecom.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to reserve an AVAILABLE resource for an order")
public record ReserveRequest(
    @NotNull @Schema(example = "1") Long resourceId,
    @NotNull @Schema(example = "2") Long orderId,
    @Schema(example = "1") Long customerId,
    @Min(1) @Schema(description = "Hold duration in minutes", example = "30") Integer ttlMinutes) {}
