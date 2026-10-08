package com.telecom.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request to replace all items on a CREATED/VALIDATED order")
public record ModifyOrderRequest(@NotEmpty @Valid List<OrderItemRequest> items) {}
