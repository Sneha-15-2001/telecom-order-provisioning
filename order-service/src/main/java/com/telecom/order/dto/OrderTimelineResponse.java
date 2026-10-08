package com.telecom.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "User-friendly order timeline (history + payment events)")
public record OrderTimelineResponse(Long orderId, String orderNumber, List<TimelineEvent> events) {

  public record TimelineEvent(LocalDateTime timestamp, String title, String detail) {}
}
