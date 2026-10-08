package com.telecom.order.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI definition for Order Service.
 *
 * <p>Phase 1: documents the foundation contract (system endpoints).
 * Domain DTOs are added from Phase 3 onwards.
 */
@OpenAPIDefinition(
    info =
        @Info(
            title = "Order Service API",
            version = "0.0.1",
            description =
                "Telecom Order Provisioning - Order Service. "
                    + "Primary business orchestration: create/validate/submit/track/modify/cancel/retry "
                    + "orders, payments, promotions, bulk and corporate orders. "
                    + "Phase 1 exposes only the foundation contract."))
@Configuration
public class OpenApiConfig {}
