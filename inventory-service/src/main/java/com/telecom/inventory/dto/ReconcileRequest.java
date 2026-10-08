package com.telecom.inventory.dto;

import com.telecom.inventory.entity.ResourceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Admin correction of a resource status (used for data-incident recovery)")
public record ReconcileRequest(
    @NotNull @Schema(example = "1") Long resourceId,
    @NotNull @Schema(example = "AVAILABLE") ResourceStatus actualStatus,
    @NotBlank @Schema(example = "Field audit shows SIM free in HLR") String reason) {}
