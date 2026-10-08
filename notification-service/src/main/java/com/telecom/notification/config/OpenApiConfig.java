package com.telecom.notification.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI definition for Notification Service.
 *
 * <p>Phase 1: documents the foundation contract (system endpoints).
 * Domain DTOs are added from Phase 6 onwards. SMS/email delivery is
 * simulated; no real provider integration in early phases.
 */
@OpenAPIDefinition(
    info =
        @Info(
            title = "Notification Service API",
            version = "0.0.1",
            description =
                "Telecom Order Provisioning - Notification Service. "
                    + "Owns order/provisioning notifications, simulated SMS/email, "
                    + "templates and retry. "
                    + "Phase 1 exposes only the foundation contract."))
@Configuration
public class OpenApiConfig {}
