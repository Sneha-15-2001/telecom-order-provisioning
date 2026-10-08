package com.telecom.provisioning.dto;

import com.telecom.provisioning.entity.ProvisioningStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "One immutable provisioning history entry")
public record ProvisioningHistoryEntry(
    Long id, String event, ProvisioningStatus fromStatus, ProvisioningStatus toStatus,
    String comment, LocalDateTime timestamp) {}
