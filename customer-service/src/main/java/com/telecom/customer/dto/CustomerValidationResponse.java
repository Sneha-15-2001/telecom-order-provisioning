package com.telecom.customer.dto;

import com.telecom.customer.entity.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Result of validating a customer for order placement (Phase 7 contract)")
public record CustomerValidationResponse(
    Long customerId,
    String customerNumber,
    CustomerStatus status,
    boolean valid,
    List<String> reasons) {}
