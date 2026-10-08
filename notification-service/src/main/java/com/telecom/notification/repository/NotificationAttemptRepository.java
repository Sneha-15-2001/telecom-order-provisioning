package com.telecom.notification.repository;

import com.telecom.notification.entity.NotificationAttempt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationAttemptRepository extends JpaRepository<NotificationAttempt, Long> {

  List<NotificationAttempt> findByNotificationIdOrderByAttemptNoAsc(Long notificationId);
}
