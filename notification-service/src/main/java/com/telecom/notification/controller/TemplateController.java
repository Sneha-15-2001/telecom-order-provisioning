package com.telecom.notification.controller;

import com.telecom.notification.dto.CreateTemplateRequest;
import com.telecom.notification.dto.TemplateResponse;
import com.telecom.notification.service.TemplateService;
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

/** Message template catalog (Phase 6). */
@Tag(name = "Templates", description = "Message templates (Phase 6)")
@RestController
@RequestMapping(path = "/api/notification-templates", produces = MediaType.APPLICATION_JSON_VALUE)
public class TemplateController {

  private final TemplateService templateService;

  public TemplateController(TemplateService templateService) {
    this.templateService = templateService;
  }

  @Operation(summary = "Create a message template")
  @ApiResponse(responseCode = "201", description = "Template created")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public TemplateResponse create(@Valid @RequestBody CreateTemplateRequest request) {
    return templateService.create(request);
  }

  @Operation(summary = "List templates")
  @GetMapping
  public List<TemplateResponse> list() {
    return templateService.list();
  }

  @Operation(summary = "Get a template by ID")
  @GetMapping("/{id}")
  public TemplateResponse get(@PathVariable Long id) {
    return templateService.get(id);
  }

  @Operation(summary = "Get a template by code")
  @GetMapping("/code/{code}")
  public TemplateResponse getByCode(@PathVariable String code) {
    return templateService.getByCode(code);
  }

  @Operation(summary = "Deactivate a template")
  @PostMapping("/{id}/deactivate")
  public TemplateResponse deactivate(@PathVariable Long id) {
    return templateService.setActive(id, false);
  }

  @Operation(summary = "Reactivate a template")
  @PostMapping("/{id}/activate")
  public TemplateResponse activate(@PathVariable Long id) {
    return templateService.setActive(id, true);
  }
}
