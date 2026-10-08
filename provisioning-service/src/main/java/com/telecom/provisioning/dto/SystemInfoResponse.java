package com.telecom.provisioning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Response DTO for {@code GET /api/system/info}.
 *
 * <p>Entities are never exposed directly; request/response DTOs are the
 * contract. This DTO establishes the pattern for Phase 5+ domain DTOs.
 */
@Schema(description = "Basic runtime information about this service instance")
public record SystemInfoResponse(
    @Schema(description = "Logical service name", example = "provisioning-service")
        String service,
    @Schema(description = "Application version", example = "0.0.1-SNAPSHOT")
        String version,
    @Schema(description = "HTTP port this instance listens on", example = "8084")
        int port,
    @Schema(description = "Current server time", example = "2026-10-08T10:15:30Z")
        Instant timestamp,
    @Schema(description = "Correlation ID of the current request", example = "ABC123")
        String correlationId) {}
