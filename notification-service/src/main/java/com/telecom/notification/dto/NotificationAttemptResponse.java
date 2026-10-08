package com.telecom.notification.dto;

import com.telecom.notification.entity.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "One recorded delivery attempt")
public record NotificationAttemptResponse(
    Long id, int attemptNo, NotificationStatus status,
    String providerResponse, LocalDateTime timestamp) {}
