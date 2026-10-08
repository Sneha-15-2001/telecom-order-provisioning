package com.telecom.inventory.controller;

import com.telecom.inventory.dto.CreateResourceRequest;
import com.telecom.inventory.dto.QuarantineRequest;
import com.telecom.inventory.dto.ReconcileRequest;
import com.telecom.inventory.dto.ResourceHistoryEntry;
import com.telecom.inventory.dto.ResourceResponse;
import com.telecom.inventory.dto.UpdateResourceRequest;
import com.telecom.inventory.entity.ResourceStatus;
import com.telecom.inventory.entity.ResourceType;
import com.telecom.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Telecom resource catalog and lifecycle (Phase 4). */
@Tag(name = "Inventory", description = "Resource catalog and lifecycle (Phase 4)")
@RestController
@RequestMapping(path = "/api/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
public class InventoryController {

  private final InventoryService inventoryService;

  public InventoryController(InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Operation(summary = "Register a new resource")
  @ApiResponse(responseCode = "201", description = "Resource registered as AVAILABLE")
  @ApiResponse(responseCode = "400", description = "Identifier already registered")
  @PostMapping(path = "/resources", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ResourceResponse create(@Valid @RequestBody CreateResourceRequest request) {
    return inventoryService.create(request);
  }

  @Operation(summary = "List resources (paged, optional type/status filters)")
  @GetMapping("/resources")
  public Page<ResourceResponse> list(
      @RequestParam(required = false) ResourceType type,
      @RequestParam(required = false) ResourceStatus status,
      @ParameterObject Pageable pageable) {
    return inventoryService.list(type, status, pageable);
  }

  @Operation(summary = "Search resources by number, identifier or details")
  @GetMapping("/search")
  public Page<ResourceResponse> search(@RequestParam String q, @ParameterObject Pageable pageable) {
    return inventoryService.search(q, pageable);
  }

  @Operation(summary = "List AVAILABLE resources of a type (for order allocation)")
  @GetMapping("/available")
  public List<ResourceResponse> available(
      @RequestParam ResourceType type,
      @RequestParam(defaultValue = "20") int limit) {
    return inventoryService.available(type, limit);
  }

  @Operation(summary = "Get a resource by ID")
  @GetMapping("/resources/{id}")
  public ResourceResponse getById(@PathVariable Long id) {
    return inventoryService.getById(id);
  }

  @Operation(summary = "Get a resource by resource number")
  @GetMapping("/resources/number/{resourceNumber}")
  public ResourceResponse getByNumber(@PathVariable String resourceNumber) {
    return inventoryService.getByNumber(resourceNumber);
  }

  @Operation(summary = "Update resource details")
  @PutMapping(path = "/resources/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResourceResponse update(@PathVariable Long id, @Valid @RequestBody UpdateResourceRequest request) {
    return inventoryService.update(id, request);
  }

  @Operation(summary = "Delete an AVAILABLE resource")
  @DeleteMapping("/resources/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    inventoryService.delete(id);
  }

  @Operation(summary = "Resource audit trail")
  @GetMapping("/resources/{id}/history")
  public List<ResourceHistoryEntry> history(@PathVariable Long id) {
    return inventoryService.history(id);
  }

  @Operation(summary = "Quarantine a resource (suspect quality / fraud)")
  @PostMapping(path = "/quarantine", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResourceResponse quarantine(@Valid @RequestBody QuarantineRequest request) {
    return inventoryService.quarantine(request.resourceId(), request.reason());
  }

  @Operation(summary = "Release a resource from quarantine back to AVAILABLE")
  @PostMapping(path = "/release-quarantine", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResourceResponse releaseQuarantine(@Valid @RequestBody QuarantineRequest request) {
    return inventoryService.releaseQuarantine(request.resourceId(), request.reason());
  }

  @Operation(summary = "Admin-correct a wrong resource status (audited)")
  @ApiResponse(responseCode = "200", description = "Status corrected")
  @ApiResponse(responseCode = "404", description = "Resource not found")
  @PostMapping(path = "/reconcile", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResourceResponse reconcile(@Valid @RequestBody ReconcileRequest request) {
    return inventoryService.reconcile(request);
  }

  @Operation(summary = "List SIM resources")
  @GetMapping("/sim")
  public Page<ResourceResponse> sim(@ParameterObject Pageable pageable) {
    return inventoryService.list(ResourceType.SIM, null, pageable);
  }

  @Operation(summary = "List eSIM resources")
  @GetMapping("/esim")
  public Page<ResourceResponse> esim(@ParameterObject Pageable pageable) {
    return inventoryService.list(ResourceType.ESIM, null, pageable);
  }

  @Operation(summary = "List mobile numbers (MSISDN)")
  @GetMapping("/phone-numbers")
  public Page<ResourceResponse> phoneNumbers(@ParameterObject Pageable pageable) {
    return inventoryService.list(ResourceType.MSISDN, null, pageable);
  }

  @Operation(summary = "List devices (IMEI)")
  @GetMapping("/devices")
  public Page<ResourceResponse> devices(@ParameterObject Pageable pageable) {
    return inventoryService.list(ResourceType.DEVICE, null, pageable);
  }

  @Operation(summary = "List fiber ports")
  @GetMapping("/fiber-ports")
  public Page<ResourceResponse> fiberPorts(@ParameterObject Pageable pageable) {
    return inventoryService.list(ResourceType.FIBER_PORT, null, pageable);
  }
}
