package com.telecom.inventory.controller;

import com.telecom.inventory.dto.ReservationResponse;
import com.telecom.inventory.dto.ReserveRequest;
import com.telecom.inventory.entity.ReservationStatus;
import com.telecom.inventory.service.ReservationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Reservations: hold → confirm (allocate) → release (Phase 4). */
@Tag(name = "Reservations", description = "Resource holds for orders (Phase 4)")
@RestController
@RequestMapping(path = "/api/inventory", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReservationController {

  private final ReservationService reservationService;

  public ReservationController(ReservationService reservationService) {
    this.reservationService = reservationService;
  }

  @Operation(summary = "Reserve an AVAILABLE resource for an order")
  @ApiResponse(responseCode = "201", description = "Reservation created (ACTIVE)")
  @ApiResponse(responseCode = "400", description = "Resource not AVAILABLE")
  @ApiResponse(responseCode = "404", description = "Resource not found")
  @PostMapping(path = "/reserve", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ReservationResponse reserve(@Valid @RequestBody ReserveRequest request) {
    return reservationService.reserve(request);
  }

  @Operation(summary = "Confirm a reservation: resource becomes ALLOCATED")
  @PostMapping("/allocate")
  public ReservationResponse allocate(@RequestParam Long reservationId) {
    return reservationService.allocate(reservationId);
  }

  @Operation(summary = "Release a hold: reservation cancelled, resource freed")
  @PostMapping("/release")
  public ReservationResponse release(@RequestParam Long reservationId) {
    return reservationService.release(reservationId);
  }

  @Operation(summary = "List reservations (paged, optional status filter)")
  @GetMapping("/reservations")
  public Page<ReservationResponse> list(
      @RequestParam(required = false) ReservationStatus status,
      @ParameterObject Pageable pageable) {
    return reservationService.list(status, pageable);
  }

  @Operation(summary = "Get a reservation by ID")
  @GetMapping("/reservations/{id}")
  public ReservationResponse get(@PathVariable Long id) {
    return reservationService.get(id);
  }

  @Operation(summary = "List reservations for an order")
  @GetMapping("/reservations/order/{orderId}")
  public List<ReservationResponse> byOrder(@PathVariable Long orderId) {
    return reservationService.byOrder(orderId);
  }

  @Operation(summary = "Confirm a reservation (path-variable style)")
  @PostMapping("/reservations/{id}/confirm")
  public ReservationResponse confirm(@PathVariable Long id) {
    return reservationService.confirm(id);
  }

  @Operation(summary = "Cancel a reservation (path-variable style)")
  @PostMapping("/reservations/{id}/cancel")
  public ReservationResponse cancel(@PathVariable Long id) {
    return reservationService.release(id);
  }
}
