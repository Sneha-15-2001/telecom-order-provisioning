package com.telecom.provisioning.controller;

import com.telecom.provisioning.dto.CreateServiceProfileRequest;
import com.telecom.provisioning.dto.ServiceProfileResponse;
import com.telecom.provisioning.service.ServiceProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Reusable service profiles (Phase 5). */
@Tag(name = "Service Profiles", description = "Reusable provisioning profiles (Phase 5)")
@RestController
@RequestMapping(path = "/api/service-profiles", produces = MediaType.APPLICATION_JSON_VALUE)
public class ServiceProfileController {

  private final ServiceProfileService profileService;

  public ServiceProfileController(ServiceProfileService profileService) {
    this.profileService = profileService;
  }

  @Operation(summary = "Register a service profile")
  @ApiResponse(responseCode = "201", description = "Profile created")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ServiceProfileResponse create(@Valid @RequestBody CreateServiceProfileRequest request) {
    return profileService.create(request);
  }

  @Operation(summary = "List service profiles")
  @GetMapping
  public List<ServiceProfileResponse> list() {
    return profileService.list();
  }

  @Operation(summary = "Get a service profile by ID")
  @GetMapping("/{id}")
  public ServiceProfileResponse get(@PathVariable Long id) {
    return profileService.get(id);
  }
}
