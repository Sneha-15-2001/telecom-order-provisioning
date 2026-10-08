package com.telecom.customer.dto;

import com.telecom.customer.entity.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to change a subscription status")
public record SubscriptionStatusUpdateRequest(
    @NotNull @Schema(example = "SUSPENDED") SubscriptionStatus status) {}
