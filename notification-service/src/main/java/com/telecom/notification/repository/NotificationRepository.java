package com.telecom.notification.repository;

import com.telecom.notification.entity.Notification;
import com.telecom.notification.entity.NotificationChannel;
import com.telecom.notification.entity.NotificationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Optional<Notification> findByNotificationNumber(String notificationNumber);

  Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

  Page<Notification> findByChannel(NotificationChannel channel, Pageable pageable);

  Page<Notification> findByStatusAndChannel(NotificationStatus status, NotificationChannel channel, Pageable pageable);

  List<Notification> findByOrderIdOrderByCreatedAtDesc(Long orderId);

  List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
