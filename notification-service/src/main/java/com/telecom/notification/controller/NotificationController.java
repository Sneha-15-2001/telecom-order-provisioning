package com.telecom.notification.controller;

import com.telecom.notification.dto.CreateNotificationRequest;
import com.telecom.notification.dto.NotificationAttemptResponse;
import com.telecom.notification.dto.NotificationResponse;
import com.telecom.notification.dto.NotifyEventRequest;
import com.telecom.notification.dto.RenderPreviewRequest;
import com.telecom.notification.entity.NotificationChannel;
import com.telecom.notification.entity.NotificationStatus;
import com.telecom.notification.service.NotificationService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Simulated SMS/email/push notifications (Phase 6). No real providers. */
@Tag(name = "Notifications", description = "Simulated notifications (Phase 6)")
@RestController
@RequestMapping(path = "/api/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
public class NotificationController {

  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Operation(summary = "Queue a notification (template-rendered or free body)")
  @ApiResponse(responseCode = "201", description = "Notification queued in PENDING")
  @ApiResponse(responseCode = "400", description = "No body/template or unknown template")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public NotificationResponse create(@Valid @RequestBody CreateNotificationRequest request) {
    return notificationService.create(request);
  }

  @Operation(summary = "One-call order event notification (event code picks the template)")
  @PostMapping(path = "/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public NotificationResponse notifyEvent(@Valid @RequestBody NotifyEventRequest request) {
    return notificationService.notifyEvent(request);
  }

  @Operation(summary = "Preview a rendered template (no state change)")
  @PostMapping(path = "/render-preview", consumes = MediaType.APPLICATION_JSON_VALUE)
  public String renderPreview(@Valid @RequestBody RenderPreviewRequest request) {
    return notificationService.renderPreview(request);
  }

  @Operation(summary = "List notifications (paged, optional status/channel filters)")
  @GetMapping
  public Page<NotificationResponse> list(
      @RequestParam(required = false) NotificationStatus status,
      @RequestParam(required = false) NotificationChannel channel,
      @ParameterObject Pageable pageable) {
    return notificationService.list(status, channel, pageable);
  }

  @Operation(summary = "List failed notifications")
  @GetMapping("/failed")
  public Page<NotificationResponse> failed(@ParameterObject Pageable pageable) {
    return notificationService.list(NotificationStatus.FAILED, null, pageable);
  }

  @Operation(summary = "List pending notifications")
  @GetMapping("/pending")
  public Page<NotificationResponse> pending(@ParameterObject Pageable pageable) {
    return notificationService.list(NotificationStatus.PENDING, null, pageable);
  }

  @Operation(summary = "Get a notification by ID")
  @GetMapping("/{id}")
  public NotificationResponse get(@PathVariable Long id) {
    return notificationService.get(id);
  }

  @Operation(summary = "List notifications for an order")
  @GetMapping("/order/{orderId}")
  public List<NotificationResponse> byOrder(@PathVariable Long orderId) {
    return notificationService.byOrder(orderId);
  }

  @Operation(summary = "List notifications for a customer")
  @GetMapping("/customer/{customerId}")
  public List<NotificationResponse> byCustomer(@PathVariable Long customerId) {
    return notificationService.byCustomer(customerId);
  }

  @Operation(summary = "Delivery attempts of a notification")
  @GetMapping("/{id}/attempts")
  public List<NotificationAttemptResponse> attempts(@PathVariable Long id) {
    return notificationService.attempts(id);
  }

  @Operation(summary = "Run simulated delivery (→ SENT, or FAILED for fail-test recipients)")
  @ApiResponse(responseCode = "200", description = "Delivery attempted; status carries the verdict")
  @ApiResponse(responseCode = "400", description = "Notification not PENDING/RETRYING")
  @PostMapping("/{id}/send")
  public NotificationResponse send(@PathVariable Long id) {
    return notificationService.send(id);
  }

  @Operation(summary = "Retry a failed notification (re-runs simulated delivery)")
  @PostMapping("/{id}/retry")
  public NotificationResponse retry(@PathVariable Long id) {
    return notificationService.retry(id);
  }

  @Operation(summary = "Cancel a notification (not SENT ones)")
  @PostMapping("/{id}/cancel")
  public NotificationResponse cancel(@PathVariable Long id) {
    return notificationService.cancel(id);
  }

  @Operation(summary = "Delete a PENDING/CANCELLED notification")
  @ApiResponse(responseCode = "204", description = "Notification deleted")
  @ApiResponse(responseCode = "400", description = "Notification already sent/failed")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    notificationService.delete(id);
  }
}
