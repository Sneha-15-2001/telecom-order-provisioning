package com.telecom.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.telecom.notification.entity.Notification;
import com.telecom.notification.entity.NotificationChannel;
import com.telecom.notification.entity.NotificationStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class NotificationRepositoryTest {

  @Autowired NotificationRepository notifications;

  @Test
  void persistsAndFindsByNumber() {
    Notification n = new Notification();
    n.setNotificationNumber("NTF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    n.setChannel(NotificationChannel.EMAIL);
    n.setRecipient("user@example.com");
    n.setBody("hello");
    n.setStatus(NotificationStatus.PENDING);
    notifications.saveAndFlush(n);

    assertThat(notifications.findByNotificationNumber(n.getNotificationNumber())).isPresent();
  }
}
