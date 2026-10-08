package com.telecom.notification.mapper;

import com.telecom.notification.dto.NotificationAttemptResponse;
import com.telecom.notification.dto.NotificationResponse;
import com.telecom.notification.dto.TemplateResponse;
import com.telecom.notification.entity.Notification;
import com.telecom.notification.entity.NotificationAttempt;
import com.telecom.notification.entity.NotificationTemplate;

/** Explicit entity → DTO mapping. */
public final class NotificationMapper {

  private NotificationMapper() {}

  public static NotificationResponse toResponse(Notification n) {
    return new NotificationResponse(n.getId(), n.getNotificationNumber(), n.getOrderId(),
        n.getCustomerId(), n.getChannel(), n.getRecipient(), n.getTemplateCode(),
        n.getSubject(), n.getBody(), n.getStatus(), n.getAttempts(), n.getLastError(),
        n.getProviderMessageId(), n.getCreatedAt(), n.getUpdatedAt());
  }

  public static NotificationAttemptResponse toResponse(NotificationAttempt a) {
    return new NotificationAttemptResponse(a.getId(), a.getAttemptNo(), a.getStatus(),
        a.getProviderResponse(), a.getCreatedAt());
  }

  public static TemplateResponse toResponse(NotificationTemplate t) {
    return new TemplateResponse(t.getId(), t.getTemplateCode(), t.getChannel(),
        t.getSubjectTemplate(), t.getBodyTemplate(), t.isActive());
  }
}
