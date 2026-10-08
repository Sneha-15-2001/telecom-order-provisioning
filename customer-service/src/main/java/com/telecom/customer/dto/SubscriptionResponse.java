package com.telecom.customer.dto;

import com.telecom.customer.entity.SubscriptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Customer subscription")
public record SubscriptionResponse(
    Long id,
    Long customerId,
    String subscriptionNumber,
    String planCode,
    String planName,
    String msisdn,
    SubscriptionStatus status,
    LocalDate startDate,
    LocalDate endDate,
    LocalDateTime createdAt) {}
