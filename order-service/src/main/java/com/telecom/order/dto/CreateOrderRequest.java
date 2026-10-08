package com.telecom.order.dto;

import com.telecom.order.entity.OrderPriority;
import com.telecom.order.entity.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Request to create an order")
public record CreateOrderRequest(
    @NotNull @Schema(example = "1") Long customerId,
    @Schema(example = "CUS-DEMO001") String customerNumber,
    @NotNull @Schema(example = "NEW_CONNECTION") OrderType orderType,
    @Schema(example = "NORMAL") OrderPriority priority,
    @Size(max = 1000) String notes,
    @NotEmpty @Valid List<OrderItemRequest> items) {}
