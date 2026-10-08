package com.telecom.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to quarantine / release-quarantine a resource")
public record QuarantineRequest(
    @NotNull @Schema(example = "1") Long resourceId,
    @NotBlank @Schema(example = "SIM failed QA batch check") String reason) {}
