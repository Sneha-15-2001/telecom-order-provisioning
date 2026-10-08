package com.telecom.order.integration;

import com.telecom.order.integration.dto.NotificationNotifyRequest;
import com.telecom.order.integration.dto.NotificationSendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/** Synchronous REST client for notification-service (order → notification). Used by fulfillment in Phase 10. */
@Component
public class NotificationServiceClient {

  private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);

  private final WebClient webClient;

  public NotificationServiceClient(WebClient notificationWebClient) {
    this.webClient = notificationWebClient;
  }

  public NotificationSendResult notify(NotificationNotifyRequest req) {
    NotificationSendResult res = webClient.post()
        .uri("/api/notifications/notify")
        .bodyValue(req)
        .retrieve()
        .bodyToMono(NotificationSendResult.class)
        .block();
    log.info("event=NOTIFICATION_NOTIFY_CALL orderId={} notification={}",
        req.orderId(), res != null ? res.notificationNumber() : null);
    return res;
  }
}
