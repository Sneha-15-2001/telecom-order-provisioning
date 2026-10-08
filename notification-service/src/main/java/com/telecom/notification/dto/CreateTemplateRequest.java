package com.telecom.notification.dto;

import com.telecom.notification.entity.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a message template")
public record CreateTemplateRequest(
    @NotBlank @Size(max = 50) @Schema(example = "ORDER_CREATED") String templateCode,
    @NotNull @Schema(example = "SMS") NotificationChannel channel,
    @Size(max = 255) @Schema(example = "Order {{orderNumber}} received") String subjectTemplate,
    @NotBlank @Size(max = 4000)
        @Schema(example = "Hi, your order {{orderNumber}} ({{planName}}) is {{status}}. - Telecom")
        String bodyTemplate,
    @Schema(example = "true") boolean active) {}
