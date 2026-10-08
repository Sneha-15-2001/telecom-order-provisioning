package com.telecom.order.dto;

import com.telecom.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current lifecycle status of an order")
public record OrderStatusResponse(Long orderId, String orderNumber, OrderStatus status) {}
