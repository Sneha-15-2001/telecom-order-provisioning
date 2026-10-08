package com.telecom.order.controller;

import com.telecom.order.dto.CreatePromotionRequest;
import com.telecom.order.dto.PromotionResponse;
import com.telecom.order.service.PromotionService;
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

/** Promotion catalog (Phase 3). */
@Tag(name = "Promotions", description = "Promotion catalog (Phase 3)")
@RestController
@RequestMapping(path = "/api/promotions", produces = MediaType.APPLICATION_JSON_VALUE)
public class PromotionController {

  private final PromotionService promotionService;

  public PromotionController(PromotionService promotionService) {
    this.promotionService = promotionService;
  }

  @Operation(summary = "Add a promotion to the catalog")
  @ApiResponse(responseCode = "201", description = "Promotion created")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public PromotionResponse create(@Valid @RequestBody CreatePromotionRequest request) {
    return promotionService.create(request);
  }

  @Operation(summary = "List promotions")
  @GetMapping
  public List<PromotionResponse> list() {
    return promotionService.list();
  }

  @Operation(summary = "Get a promotion by ID")
  @GetMapping("/{id}")
  public PromotionResponse get(@PathVariable Long id) {
    return promotionService.get(id);
  }
}
