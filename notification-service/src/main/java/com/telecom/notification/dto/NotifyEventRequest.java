package com.telecom.notification.dto;

import com.telecom.notification.entity.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

@Schema(description = "One-call order event notification (template picked by event code)")
public record NotifyEventRequest(
    @NotNull @Schema(example = "2") Long orderId,
    @Schema(example = "1") Long customerId,
    @NotNull @Schema(example = "SMS") NotificationChannel channel,
    @NotBlank @Size(max = 255) @Schema(example = "+919876543210") String recipient,
    @NotBlank @Schema(example = "ORDER_CREATED") String event,
    Map<String, String> variables) {}
