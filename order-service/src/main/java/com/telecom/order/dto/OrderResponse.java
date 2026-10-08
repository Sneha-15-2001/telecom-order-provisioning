package com.telecom.order.dto;

import com.telecom.order.entity.OrderPriority;
import com.telecom.order.entity.OrderStatus;
import com.telecom.order.entity.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Order with items and payable total")
public record OrderResponse(
    Long id,
    String orderNumber,
    Long customerId,
    String customerNumber,
    OrderType orderType,
    OrderStatus status,
    OrderPriority priority,
    BigDecimal totalAmount,
    BigDecimal discountAmount,
    BigDecimal payableAmount,
    String promoCode,
    String notes,
    List<OrderItemResponse> items,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
