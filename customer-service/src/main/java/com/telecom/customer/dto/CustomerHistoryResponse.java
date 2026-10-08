package com.telecom.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Simplified customer timeline (created + subscription events)")
public record CustomerHistoryResponse(
    Long customerId, List<HistoryEvent> events) {

  @Schema(description = "One timeline entry")
  public record HistoryEvent(String type, String description, LocalDateTime timestamp) {}
}
