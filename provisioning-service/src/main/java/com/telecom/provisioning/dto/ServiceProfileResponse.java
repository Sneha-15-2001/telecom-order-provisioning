package com.telecom.provisioning.dto;

import com.telecom.provisioning.entity.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A reusable service profile")
public record ServiceProfileResponse(
    Long id, String profileCode, ServiceType serviceType,
    String description, String config, boolean active) {}
