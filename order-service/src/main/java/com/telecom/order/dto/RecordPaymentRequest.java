package com.telecom.order.dto;

import com.telecom.order.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request to record a payment attempt against a PAYMENT_PENDING order")
public record RecordPaymentRequest(
    @NotNull @DecimalMin("0.01") @Schema(example = "299.00") BigDecimal amount,
    @NotNull @Schema(example = "UPI") PaymentMethod method) {}
