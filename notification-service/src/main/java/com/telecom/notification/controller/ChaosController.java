package com.telecom.notification.controller;

import com.telecom.notification.config.ChaosSwitch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Fault injection for platform-failure scenarios. Only active when
 * {@code CHAOS_ENABLED=true}; otherwise every call is refused with 404 so the
 * endpoint does not advertise itself.
 */
@RestController
@RequestMapping(path = "/api/chaos", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Chaos", description = "Dev-only fault injection (disabled by default)")
public class ChaosController {

  private final ChaosSwitch chaos;

  public ChaosController(ChaosSwitch chaos) {
    this.chaos = chaos;
  }

  @Operation(summary = "Arm a fault on one path prefix for N seconds")
  @PostMapping("/fault")
  public ResponseEntity<?> arm(
      @RequestParam String path,
      @RequestParam int status,
      @RequestParam(defaultValue = "30") int seconds,
      HttpServletRequest request) {
    if (!chaos.isEnabled()) {
      return notFound(request);
    }
    chaos.arm(path, status, seconds);
    return ResponseEntity.ok(Map.of(
        "armed", path, "status", status, "seconds", seconds,
        "note", "Every matching request now fails until this expires"));
  }

  @Operation(summary = "Clear armed faults (path=* for all)")
  @DeleteMapping("/fault")
  public ResponseEntity<?> clear(
      @RequestParam(defaultValue = "*") String path, HttpServletRequest request) {
    if (!chaos.isEnabled()) {
      return notFound(request);
    }
    chaos.clear(path);
    return ResponseEntity.ok(Map.of("cleared", path));
  }

  @Operation(summary = "Which faults are armed and how many times they fired")
  @GetMapping("/status")
  public ResponseEntity<?> status(HttpServletRequest request) {
    if (!chaos.isEnabled()) {
      return notFound(request);
    }
    return ResponseEntity.ok(chaos.status());
  }

  private ResponseEntity<?> notFound(HttpServletRequest request) {
    return ResponseEntity.status(404).body(Map.of(
        "status", 404,
        "error", "NOT_FOUND",
        "message", "Chaos injection is disabled. Set CHAOS_ENABLED=true to arm faults.",
        "path", request.getRequestURI(),
        "correlationId", String.valueOf(MDC.get("correlationId"))));
  }
}
