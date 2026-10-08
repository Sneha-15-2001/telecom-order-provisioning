package com.telecom.order.integration.dto;

import java.util.Map;

/** Wire shape of notification-service POST /api/notifications/notify. */
public record NotificationNotifyRequest(
    Long orderId, Long customerId, String channel, String recipient, String event, Map<String, String> variables) {}
