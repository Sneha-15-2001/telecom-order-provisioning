package com.telecom.provisioning.dto;

import com.telecom.provisioning.entity.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to open a provisioning request for an order")
public record CreateProvisioningRequest(
    @NotNull @Schema(example = "2") Long orderId,
    @Schema(example = "1") Long customerId,
    @NotNull @Schema(example = "MOBILE") ServiceType serviceType,
    @Pattern(regexp = "^[0-9]{7,15}$", message = "must be 7-15 digits")
        @Schema(example = "919876543210") String msisdn,
    @Size(max = 20) @Schema(example = "RES-SIM0001") String resourceNumber,
    @Size(max = 50) @Schema(example = "PLAN_5G_299") String planCode) {}
