package com.telecom.order.integration.dto;

/** Wire shape of provisioning-service request responses (status as String). */
public record ProvisioningRequestResult(
    Long id, String requestNumber, Long orderId, String serviceType, String status, String lastError) {}
