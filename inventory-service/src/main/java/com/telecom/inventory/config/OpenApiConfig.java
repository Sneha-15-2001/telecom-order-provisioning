package com.telecom.inventory.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI definition for Inventory Service.
 *
 * <p>Phase 1: documents the foundation contract (system endpoints).
 * Domain DTOs are added from Phase 4 onwards.
 */
@OpenAPIDefinition(
    info =
        @Info(
            title = "Inventory Service API",
            version = "0.0.1",
            description =
                "Telecom Order Provisioning - Inventory Service. "
                    + "Owns SIM/eSIM/MSISDN/device/IMEI/fiber-port/ONT/router resources: "
                    + "availability, reserve, allocate, release, reconcile, quarantine. "
                    + "Phase 1 exposes only the foundation contract."))
@Configuration
public class OpenApiConfig {}
