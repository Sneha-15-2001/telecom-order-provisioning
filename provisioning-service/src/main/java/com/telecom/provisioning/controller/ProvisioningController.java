package com.telecom.provisioning.controller;

import com.telecom.provisioning.dto.CreateProvisioningRequest;
import com.telecom.provisioning.dto.ProvisioningHistoryEntry;
import com.telecom.provisioning.dto.ProvisioningResponse;
import com.telecom.provisioning.entity.ProvisioningStatus;
import com.telecom.provisioning.entity.ServiceType;
import com.telecom.provisioning.service.ProvisioningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Simulated network/service provisioning (Phase 5). No real network elements. */
@Tag(name = "Provisioning", description = "Simulated provisioning lifecycle (Phase 5)")
@Validated
@RestController
@RequestMapping(path = "/api/provisioning", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProvisioningController {

  private final ProvisioningService provisioningService;

  public ProvisioningController(ProvisioningService provisioningService) {
    this.provisioningService = provisioningService;
  }

  @Operation(summary = "Open a provisioning request for an order")
  @ApiResponse(responseCode = "201", description = "Request created in PENDING")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ProvisioningResponse create(@Valid @RequestBody CreateProvisioningRequest request) {
    return provisioningService.create(request);
  }

  @Operation(summary = "Open a mobile activation request")
  @PostMapping(path = "/mobile")
  @ResponseStatus(HttpStatus.CREATED)
  public ProvisioningResponse mobile(
      @RequestParam Long orderId,
      @RequestParam(required = false) Long customerId,
      @RequestParam @Pattern(regexp = "^[0-9]{7,15}$") String msisdn,
      @RequestParam(required = false) String planCode) {
    return provisioningService.create(
        new CreateProvisioningRequest(orderId, customerId, ServiceType.MOBILE, msisdn, null, planCode));
  }

  @Operation(summary = "Open an eSIM provisioning request")
  @PostMapping(path = "/esim")
  @ResponseStatus(HttpStatus.CREATED)
  public ProvisioningResponse esim(
      @RequestParam Long orderId,
      @RequestParam(required = false) Long customerId,
      @RequestParam @Pattern(regexp = "^[0-9]{7,15}$") String msisdn,
      @RequestParam(required = false) String planCode) {
    return provisioningService.create(
        new CreateProvisioningRequest(orderId, customerId, ServiceType.ESIM, msisdn, null, planCode));
  }

  @Operation(summary = "Open a broadband activation request")
  @PostMapping(path = "/broadband")
  @ResponseStatus(HttpStatus.CREATED)
  public ProvisioningResponse broadband(
      @RequestParam Long orderId,
      @RequestParam(required = false) Long customerId,
      @RequestParam String resourceNumber,
      @RequestParam(required = false) String planCode) {
    return provisioningService.create(
        new CreateProvisioningRequest(orderId, customerId, ServiceType.BROADBAND, null, resourceNumber, planCode));
  }

  @Operation(summary = "Open a roaming activation request")
  @PostMapping(path = "/roaming")
  @ResponseStatus(HttpStatus.CREATED)
  public ProvisioningResponse roaming(
      @RequestParam Long orderId,
      @RequestParam(required = false) Long customerId,
      @RequestParam @Pattern(regexp = "^[0-9]{7,15}$") String msisdn) {
    return provisioningService.create(
        new CreateProvisioningRequest(orderId, customerId, ServiceType.ROAMING, msisdn, null, null));
  }

  @Operation(summary = "List provisioning requests (paged, optional type/status filters)")
  @GetMapping
  public Page<ProvisioningResponse> list(
      @RequestParam(required = false) ServiceType serviceType,
      @RequestParam(required = false) ProvisioningStatus status,
      @ParameterObject Pageable pageable) {
    return provisioningService.list(serviceType, status, pageable);
  }

  @Operation(summary = "List failed provisioning requests")
  @GetMapping("/failed")
  public Page<ProvisioningResponse> failed(@ParameterObject Pageable pageable) {
    return provisioningService.list(null, ProvisioningStatus.FAILED, pageable);
  }

  @Operation(summary = "List pending provisioning requests")
  @GetMapping("/pending")
  public Page<ProvisioningResponse> pending(@ParameterObject Pageable pageable) {
    return provisioningService.list(null, ProvisioningStatus.PENDING, pageable);
  }

  @Operation(summary = "Get a provisioning request by ID")
  @GetMapping("/{id}")
  public ProvisioningResponse get(@PathVariable Long id) {
    return provisioningService.get(id);
  }

  @Operation(summary = "Current status of a provisioning request")
  @GetMapping("/{id}/status")
  public ProvisioningResponse status(@PathVariable Long id) {
    return provisioningService.get(id);
  }

  @Operation(summary = "Audit trail of a provisioning request")
  @GetMapping("/{id}/history")
  public List<ProvisioningHistoryEntry> history(@PathVariable Long id) {
    return provisioningService.history(id);
  }

  @Operation(summary = "List provisioning requests for an order")
  @GetMapping("/order/{orderId}")
  public List<ProvisioningResponse> byOrder(@PathVariable Long orderId) {
    return provisioningService.byOrder(orderId);
  }

  @Operation(summary = "Pre-flight identifier check (no state change)")
  @PostMapping("/{id}/validate")
  public ProvisioningResponse validate(@PathVariable Long id) {
    return provisioningService.validate(id);
  }

  @Operation(summary = "Start a pending request (→ IN_PROGRESS)")
  @PostMapping("/{id}/start")
  public ProvisioningResponse start(@PathVariable Long id) {
    return provisioningService.start(id);
  }

  @Operation(summary = "Run simulated activation (→ COMPLETED, or FAILED when identifiers are missing)")
  @ApiResponse(responseCode = "200", description = "Activation attempted; status carries the verdict")
  @ApiResponse(responseCode = "400", description = "Request not IN_PROGRESS")
  @PostMapping("/{id}/activate")
  public ProvisioningResponse activate(@PathVariable Long id) {
    return provisioningService.activate(id);
  }

  @Operation(summary = "Deactivate a completed service (→ CANCELLED)")
  @PostMapping("/{id}/deactivate")
  public ProvisioningResponse deactivate(@PathVariable Long id) {
    return provisioningService.deactivate(id);
  }

  @Operation(summary = "Retry a failed request (→ PENDING)")
  @PostMapping("/{id}/retry")
  public ProvisioningResponse retry(@PathVariable Long id) {
    return provisioningService.retry(id);
  }

  @Operation(summary = "Reprocess a failed/cancelled request from scratch (→ PENDING)")
  @PostMapping("/{id}/reprocess")
  public ProvisioningResponse reprocess(@PathVariable Long id) {
    return provisioningService.reprocess(id);
  }

  @Operation(summary = "Cancel a request")
  @PostMapping("/{id}/cancel")
  public ProvisioningResponse cancel(@PathVariable Long id) {
    return provisioningService.cancel(id);
  }

  @Operation(summary = "Roll back a failed/in-flight request (→ ROLLED_BACK)")
  @PostMapping("/{id}/rollback")
  public ProvisioningResponse rollback(@PathVariable Long id) {
    return provisioningService.rollback(id);
  }
}
