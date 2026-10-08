package com.telecom.order.integration.dto;

/** Wire shape of customer-service GET /api/customers/{id} (subset used by fulfillment). */
public record CustomerProfile(Long id, String customerNumber, String firstName, String lastName,
    String email, String phone) {}
