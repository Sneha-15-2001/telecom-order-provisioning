package com.telecom.order.integration.dto;

/** Wire shape of provisioning-service POST /api/provisioning. */
public record ProvisioningCreateRequest(
    Long orderId, Long customerId, String serviceType, String msisdn, String resourceNumber, String planCode) {}
