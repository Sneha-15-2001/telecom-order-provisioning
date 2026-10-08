package com.telecom.order.dto;

import com.telecom.order.entity.PaymentMethod;
import com.telecom.order.entity.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "A payment attempt on an order")
public record PaymentResponse(
    Long id,
    Long orderId,
    String paymentReference,
    BigDecimal amount,
    PaymentMethod method,
    PaymentStatus status,
    LocalDateTime createdAt) {}
