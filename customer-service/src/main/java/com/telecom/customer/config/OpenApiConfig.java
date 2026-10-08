package com.telecom.customer.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI definition for Customer Service.
 *
 * <p>Phase 1: documents the foundation contract (system endpoints).
 * Domain DTOs are added from Phase 2 onwards.
 */
@OpenAPIDefinition(
    info =
        @Info(
            title = "Customer Service API",
            version = "0.0.1",
            description =
                "Telecom Order Provisioning - Customer Service. "
                    + "Owns customer onboarding, profile, address, status, eligibility, "
                    + "subscriptions and history. Phase 1 exposes only the foundation contract."))
@Configuration
public class OpenApiConfig {}
