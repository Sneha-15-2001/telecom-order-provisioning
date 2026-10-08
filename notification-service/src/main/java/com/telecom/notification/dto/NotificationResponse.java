package com.telecom.notification.dto;

import com.telecom.notification.entity.NotificationChannel;
import com.telecom.notification.entity.NotificationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "A queued/sent notification")
public record NotificationResponse(
    Long id,
    String notificationNumber,
    Long orderId,
    Long customerId,
    NotificationChannel channel,
    String recipient,
    String templateCode,
    String subject,
    String body,
    NotificationStatus status,
    int attempts,
    String lastError,
    String providerMessageId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
