package com.telecom.inventory.dto;

import com.telecom.inventory.entity.ResourceStatus;
import com.telecom.inventory.entity.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "A telecom resource")
public record ResourceResponse(
    Long id,
    String resourceNumber,
    ResourceType resourceType,
    String identifier,
    ResourceStatus status,
    String details,
    Long orderId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
