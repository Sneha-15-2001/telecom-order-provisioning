package com.telecom.order.integration.dto;

/** Wire shape of notification-service notification responses (status as String). */
public record NotificationSendResult(Long id, String notificationNumber, String status) {}
