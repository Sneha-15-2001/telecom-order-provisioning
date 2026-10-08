package com.telecom.provisioning.dto;

import com.telecom.provisioning.entity.ProvisioningStatus;
import com.telecom.provisioning.entity.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "A provisioning request")
public record ProvisioningResponse(
    Long id,
    String requestNumber,
    Long orderId,
    Long customerId,
    ServiceType serviceType,
    String msisdn,
    String resourceNumber,
    String planCode,
    ProvisioningStatus status,
    int attempts,
    String lastError,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
