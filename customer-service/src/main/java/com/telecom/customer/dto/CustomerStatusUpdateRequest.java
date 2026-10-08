package com.telecom.customer.dto;

import com.telecom.customer.entity.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to change a customer status (admin override)")
public record CustomerStatusUpdateRequest(
    @NotNull @Schema(example = "SUSPENDED") CustomerStatus status) {}
