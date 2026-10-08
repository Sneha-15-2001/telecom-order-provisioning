package com.telecom.order.dto;

import com.telecom.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Result of validating a recorded payment against the payable amount")
public record PaymentValidationResponse(
    Long orderId,
    String paymentReference,
    BigDecimal expectedAmount,
    BigDecimal paidAmount,
    boolean valid,
    OrderStatus orderStatus) {}
