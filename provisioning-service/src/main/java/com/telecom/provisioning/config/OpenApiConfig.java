package com.telecom.provisioning.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI definition for Provisioning Service.
 *
 * <p>Phase 1: documents the foundation contract (system endpoints).
 * Domain DTOs are added from Phase 5 onwards. No connection to any real
 * telecom network element is ever made; provisioning is simulated.
 */
@OpenAPIDefinition(
    info =
        @Info(
            title = "Provisioning Service API",
            version = "0.0.1",
            description =
                "Telecom Order Provisioning - Provisioning Service. "
                    + "Simulates mobile/eSIM/broadband/roaming activation, suspension, "
                    + "resume, deactivation and rollback. "
                    + "Phase 1 exposes only the foundation contract."))
@Configuration
public class OpenApiConfig {}
