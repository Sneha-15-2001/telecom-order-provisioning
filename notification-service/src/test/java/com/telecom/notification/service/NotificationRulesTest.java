package com.telecom.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.telecom.notification.dto.CreateTemplateRequest;
import com.telecom.notification.entity.Notification;
import com.telecom.notification.entity.NotificationChannel;
import com.telecom.notification.entity.NotificationStatus;
import com.telecom.notification.entity.NotificationTemplate;
import com.telecom.notification.repository.NotificationAttemptRepository;
import com.telecom.notification.repository.NotificationRepository;
import com.telecom.notification.repository.NotificationTemplateRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationRulesTest {

  @Mock NotificationRepository notifications;
  @Mock NotificationAttemptRepository attempts;
  @Mock NotificationTemplateRepository templates;

  @InjectMocks NotificationService notificationService;
  @InjectMocks TemplateService templateService;

  private Notification notification(NotificationStatus status) {
    Notification n = new Notification();
    n.setStatus(status);
    n.setRecipient("+911111111111");
    n.setChannel(NotificationChannel.SMS);
    n.setBody("hi");
    n.setAttempts(0);
    return n;
  }

  @Test
  void cancelSentRejected() {
    when(notifications.findById(1L)).thenReturn(Optional.of(notification(NotificationStatus.SENT)));
    assertThatThrownBy(() -> notificationService.cancel(1L)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void deletePendingAllowed() {
    when(notifications.findById(2L)).thenReturn(Optional.of(notification(NotificationStatus.PENDING)));
    when(attempts.findByNotificationIdOrderByAttemptNoAsc(2L)).thenReturn(java.util.List.of());
    notificationService.delete(2L);
  }

  @Test
  void deleteSentRejected() {
    when(notifications.findById(3L)).thenReturn(Optional.of(notification(NotificationStatus.SENT)));
    assertThatThrownBy(() -> notificationService.delete(3L)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void createRequiresBodyOrTemplate() {
    assertThatThrownBy(() -> notificationService.create(
        new com.telecom.notification.dto.CreateNotificationRequest(1L, 1L, NotificationChannel.SMS,
            "+911111111111", null, null, "  ", null)))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void templateDeactivateAndReactivate() {
    NotificationTemplate t = new NotificationTemplate();
    t.setActive(true);
    when(templates.findById(5L)).thenReturn(Optional.of(t));
    assertThat(templateService.setActive(5L, false).active()).isFalse();
    assertThat(templateService.setActive(5L, true).active()).isTrue();
  }

  @Test
  void duplicateTemplateRejected() {
    when(templates.findByTemplateCode("X")).thenReturn(Optional.of(new NotificationTemplate()));
    assertThatThrownBy(() -> templateService.create(
        new CreateTemplateRequest("X", NotificationChannel.SMS, null, "b", true)))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
