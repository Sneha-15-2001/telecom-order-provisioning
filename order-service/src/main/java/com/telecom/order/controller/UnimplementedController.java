package com.telecom.order.controller;

import com.telecom.order.dto.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Capabilities this service does not have, stated as 501 rather than hidden.
 *
 * <p>Real payment capture and real network activation are the two things this
 * system simulates. Rather than leave a client to infer that from a field named
 * "simulated", these endpoints exist and answer 501 with a precise reason.
 *
 * <p>501 is the correct status: the endpoint is understood and the method is
 * valid, but this deployment cannot support it. Returning 404 would imply the
 * route is wrong; returning 200 with a fake success would be worse than either.
 */
@RestController
@RequestMapping(path = "/api/unimplemented", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Unimplemented", description = "Capabilities that are not available in this deployment")
public class UnimplementedController {

  @Operation(summary = "Real payment-provider capture (not implemented)")
  @ApiResponse(responseCode = "501", description = "No payment gateway is integrated")
  @PostMapping("/orders/{id}/payment-capture")
  public ResponseEntity<ApiError> capture(@PathVariable Long id, HttpServletRequest request) {
    return notImplemented(
        "Real payment capture is not implemented in this deployment. "
            + "Payments are recorded and validated locally; no gateway is called, so no money moves. "
            + "Integrate a PSP (Razorpay, Stripe) and call it here before going live.",
        request);
  }

  @Operation(summary = "Real network activation (not implemented)")
  @ApiResponse(responseCode = "501", description = "No HLR/OSS adapter is integrated")
  @PostMapping("/orders/{id}/network-activation")
  public ResponseEntity<ApiError> activate(@PathVariable Long id, HttpServletRequest request) {
    return notImplemented(
        "Real network activation is not implemented in this deployment. "
            + "provisioning-service marks requests COMPLETED locally without contacting an HLR, HSS or OSS. "
            + "Add mediation and a network adapter before this can serve a live subscriber.",
        request);
  }

  @Operation(summary = "Refund an order (not implemented)")
  @ApiResponse(responseCode = "501", description = "Refunds are not supported")
  @PostMapping("/orders/{id}/refund")
  public ResponseEntity<ApiError> refund(@PathVariable Long id, HttpServletRequest request) {
    return notImplemented(
        "Refunds are not implemented. PaymentStatus.REFUNDED exists in the schema but nothing sets it. "
            + "A real refund must reverse the PSP transaction before the order can be reversed.",
        request);
  }

  private ResponseEntity<ApiError> notImplemented(String reason, HttpServletRequest request) {
    String cid = MDC.get("correlationId");
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
        .header("X-Correlation-Id", cid == null ? "" : cid)
        .body(new ApiError(Instant.now(), 501, "NOT_IMPLEMENTED", reason,
            request.getRequestURI(), cid));
  }
}
