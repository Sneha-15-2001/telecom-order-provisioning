package com.telecom.order.integration.dto;

import java.util.List;

/** Wire shape of customer-service POST /api/customers/{id}/validate (status kept as String to avoid enum coupling). */
public record CustomerValidationResult(
    Long customerId, String customerNumber, String status, boolean valid, List<String> reasons) {}
