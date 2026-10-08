package com.telecom.customer.controller;

import com.telecom.customer.dto.SystemInfoResponse;
import com.telecom.customer.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 1 foundation controller.
 *
 * <p>Domain controllers (customers, subscriptions, …) arrive in Phase 2.
 * This controller exists to verify routing, DTOs, Swagger, logging and
 * correlation-ID handling end to end.
 */
@Tag(name = "System", description = "Service foundation endpoints (Phase 1)")
@RestController
@RequestMapping(path = "/api/system", produces = MediaType.APPLICATION_JSON_VALUE)
public class SystemController {

  private final SystemService systemService;

  public SystemController(SystemService systemService) {
    this.systemService = systemService;
  }

  @Operation(summary = "Liveness probe for this service")
  @ApiResponse(responseCode = "200", description = "Service is up")
  @GetMapping("/ping")
  public Map<String, String> ping() {
    return systemService.ping();
  }

  @Operation(summary = "Runtime information about this service instance")
  @ApiResponse(responseCode = "200", description = "Service info returned")
  @GetMapping("/info")
  public SystemInfoResponse info() {
    return systemService.info();
  }
}
