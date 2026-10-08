package com.telecom.notification.dto;

import com.telecom.notification.entity.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

@Schema(description = "Request to queue a notification (template-rendered or free body)")
public record CreateNotificationRequest(
    @Schema(example = "2") Long orderId,
    @Schema(example = "1") Long customerId,
    @NotNull @Schema(example = "SMS") NotificationChannel channel,
    @NotBlank @Size(max = 255) @Schema(example = "+919876543210") String recipient,
    @Size(max = 50) @Schema(example = "ORDER_CREATED") String templateCode,
    @Size(max = 255) String subject,
    @Size(max = 4000) String body,
    @Schema(description = "{{placeholder}} values when using a template") Map<String, String> variables) {}
