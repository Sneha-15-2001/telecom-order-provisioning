package com.telecom.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Step-by-step trace of a fulfillment run. */
@Schema(description = "Result of running end-to-end fulfillment for an order")
public record FulfillmentResponse(
    Long orderId,
    String orderNumber,
    String orderStatus,
    List<FulfillmentStep> steps) {

  @Schema(description = "One saga step")
  public record FulfillmentStep(String step, String status, String detail) {}
}
