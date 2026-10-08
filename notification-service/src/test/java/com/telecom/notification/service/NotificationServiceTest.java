package com.telecom.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.telecom.notification.dto.CreateNotificationRequest;
import com.telecom.notification.dto.NotificationResponse;
import com.telecom.notification.dto.NotifyEventRequest;
import com.telecom.notification.entity.Notification;
import com.telecom.notification.entity.NotificationChannel;
import com.telecom.notification.entity.NotificationStatus;
import com.telecom.notification.entity.NotificationTemplate;
import com.telecom.notification.repository.NotificationAttemptRepository;
import com.telecom.notification.repository.NotificationRepository;
import com.telecom.notification.repository.NotificationTemplateRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock NotificationRepository notifications;
  @Mock NotificationAttemptRepository attempts;
  @Mock NotificationTemplateRepository templates;

  @InjectMocks NotificationService service;

  private Notification notification(NotificationStatus status, String recipient) {
    Notification n = new Notification();
    n.setStatus(status);
    n.setRecipient(recipient);
    n.setChannel(NotificationChannel.SMS);
    n.setBody("hello");
    n.setAttempts(0);
    return n;
  }

  @Test
  void renderSubstitutesPlaceholders() {
    assertThat(service.render("Order {{orderNumber}} is {{status}}", Map.of("orderNumber", "ORD-1", "status", "READY")))
        .isEqualTo("Order ORD-1 is READY");
  }

  @Test
  void sendSucceedsWithProviderId() {
    Notification n = notification(NotificationStatus.PENDING, "+919876543210");
    when(notifications.findById(1L)).thenReturn(Optional.of(n));

    NotificationResponse res = service.send(1L);
    assertThat(res.status()).isEqualTo(NotificationStatus.SENT);
    assertThat(res.providerMessageId()).startsWith("SIM-PROV-");
  }

  @Test
  void sendFailsForFailRecipientAndRetryResends() {
    Notification bad = notification(NotificationStatus.PENDING, "fail-test@example.com");
    when(notifications.findById(2L)).thenReturn(Optional.of(bad));

    assertThat(service.send(2L).status()).isEqualTo(NotificationStatus.FAILED);

    bad.setRecipient("+919876543210");
    NotificationResponse retried = service.retry(2L);
    assertThat(retried.status()).isEqualTo(NotificationStatus.SENT);
  }

  @Test
  void notifyEventUsesTemplateCode() {
    NotificationTemplate t = new NotificationTemplate();
    t.setTemplateCode("ORDER_CREATED");
    t.setActive(true);
    t.setBodyTemplate("order {{orderNumber}} is {{status}}");
    when(templates.findByTemplateCode("ORDER_CREATED")).thenReturn(Optional.of(t));
    when(notifications.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

    NotificationResponse res = service.notifyEvent(new NotifyEventRequest(2L, 1L,
        NotificationChannel.SMS, "+919876543210", "ORDER_CREATED",
        Map.of("orderNumber", "ORD-1", "status", "CREATED")));
    assertThat(res.body()).isEqualTo("order ORD-1 is CREATED");
    assertThat(res.status()).isEqualTo(NotificationStatus.PENDING);
  }

  @Test
  void sendRejectsSent() {
    Notification n = notification(NotificationStatus.SENT, "+919876543210");
    when(notifications.findById(3L)).thenReturn(Optional.of(n));

    assertThatThrownBy(() -> service.send(3L)).isInstanceOf(IllegalArgumentException.class);
  }
}
