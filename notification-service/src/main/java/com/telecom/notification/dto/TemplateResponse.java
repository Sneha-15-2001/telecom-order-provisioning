package com.telecom.notification.dto;

import com.telecom.notification.entity.NotificationChannel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A message template")
public record TemplateResponse(
    Long id, String templateCode, NotificationChannel channel,
    String subjectTemplate, String bodyTemplate, boolean active) {}
