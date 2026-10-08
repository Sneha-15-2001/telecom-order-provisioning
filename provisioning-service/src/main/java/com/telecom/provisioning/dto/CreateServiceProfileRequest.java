package com.telecom.provisioning.dto;

import com.telecom.provisioning.entity.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to register a reusable service profile")
public record CreateServiceProfileRequest(
    @NotBlank @Size(max = 50) @Schema(example = "PROFILE-5G-DEFAULT") String profileCode,
    @NotNull @Schema(example = "MOBILE") ServiceType serviceType,
    @NotBlank @Size(max = 500) @Schema(example = "Default 5G standalone profile") String description,
    @Size(max = 2000) @Schema(example = "{\"slice\":\"embb\",\"qos\":\"gold\"}") String config,
    @Schema(example = "true") boolean active) {}
