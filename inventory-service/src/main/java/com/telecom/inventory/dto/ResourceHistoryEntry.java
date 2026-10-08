package com.telecom.inventory.dto;

import com.telecom.inventory.entity.ResourceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "One immutable inventory history entry")
public record ResourceHistoryEntry(
    Long id, String event, ResourceStatus fromStatus, ResourceStatus toStatus,
    String comment, LocalDateTime timestamp) {}
