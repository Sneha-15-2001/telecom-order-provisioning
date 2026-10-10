package com.telecom.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Standard error response returned by {@code GlobalExceptionHandler}.
 */
@Schema(description = "Standard API error response")
public record ApiError(
    @Schema(description = "When the error occurred") Instant timestamp,
    @Schema(description = "HTTP status code", example = "400") int status,
    @Schema(description = "Machine-readable error code", example = "VALIDATION_ERROR")
        String error,
    @Schema(description = "Human-readable message") String message,
    @Schema(description = "Request path", example = "/api/notifications") String path,
    @Schema(description = "Correlation ID — trace this in the logs", example = "1a408e37-15ae-47b9-b0ad-ebf83a5770bd")
        String correlationId) {}
