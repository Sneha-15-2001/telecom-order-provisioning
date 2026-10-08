package com.telecom.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Request to add a subscription (plan / MSISDN) to a customer")
public record SubscriptionRequest(
    @NotBlank @Size(max = 50) @Schema(example = "PLAN_5G_299") String planCode,
    @NotBlank @Size(max = 150) @Schema(example = "5G Unlimited 299") String planName,
    @Pattern(regexp = "^[0-9]{7,15}$", message = "must be 7-15 digits")
        @Schema(example = "919876543210")
        String msisdn,
    @Schema(example = "2026-10-08") LocalDate startDate) {}
