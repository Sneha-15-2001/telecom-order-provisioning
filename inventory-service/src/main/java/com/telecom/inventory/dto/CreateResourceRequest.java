package com.telecom.inventory.dto;

import com.telecom.inventory.entity.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to register a new telecom resource")
public record CreateResourceRequest(
    @NotNull @Schema(example = "SIM") ResourceType resourceType,
    @NotBlank @Size(max = 100) @Schema(example = "899100000000000001") String identifier,
    @Size(max = 1000) @Schema(example = "Physical SIM, profile A") String details) {}
