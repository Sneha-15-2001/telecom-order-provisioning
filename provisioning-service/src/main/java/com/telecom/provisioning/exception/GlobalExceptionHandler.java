package com.telecom.provisioning.exception;

import com.telecom.provisioning.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Central error handling. Every error uses the same contract —
 * {@code {timestamp, status, error, message, path, correlationId}} — and every
 * response carries the correlation ID so a failure can be traced straight back
 * to the log lines that produced it.
 *
 * <p>The status distinguishes two things a client acts on differently:
 * a 400 means "change your payload"; a 409 means "re-read the resource and
 * decide" — the request was fine, the world was not in the expected state.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String CORRELATION_MDC_KEY = "correlationId";

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), request);
  }

  /**
   * A bad enum or malformed path/query value. This used to fall through to the
   * catch-all and surface as a 500, which told the caller the server had broken
   * when in fact they had sent a bad parameter.
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiError> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    String expected =
        ex.getRequiredType() != null && ex.getRequiredType().isEnum()
            ? " one of " + java.util.Arrays.toString(ex.getRequiredType().getEnumConstants())
            : " a valid value";
    return error(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER",
        "'" + ex.getName() + "' = '" + ex.getValue() + "' is not valid — expected"
            + expected, request);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiError> handleMissingParam(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
        "Required parameter '" + ex.getParameterName() + "' is missing", request);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ApiError> handleNotFound(
      NoSuchElementException ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ApiError> handleNoHandler(
      NoHandlerFoundException ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND",
        "No endpoint " + ex.getHttpMethod() + " " + ex.getRequestURL(), request);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiError> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    return error(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
        ex.getMethod() + " is not supported here; allowed: "
            + String.join(", ", ex.getSupportedMethods() == null
                ? new String[] {} : ex.getSupportedMethods()), request);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ApiError> handleMediaType(
      HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
    return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
        "Content-Type " + ex.getContentType() + " is not supported; use application/json",
        request);
  }

  /** Lifecycle violation: valid request, wrong state. */
  @ExceptionHandler(StateConflictException.class)
  public ResponseEntity<ApiError> handleStateConflict(
      StateConflictException ex, HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, ex.getCode(), ex.getMessage(), request);
  }

  /**
   * A required dependency did not answer. 503, not 4xx: the caller did nothing
   * wrong and the order is untouched, so this is retryable as-is.
   */
  @ExceptionHandler(DependencyUnavailableException.class)
  public ResponseEntity<ApiError> handleDependencyDown(
      DependencyUnavailableException ex, HttpServletRequest request) {
    org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class)
        .warn("event=DEPENDENCY_UNAVAILABLE dependency={} path={}", ex.getDependency(), request.getRequestURI());
    return error(HttpStatus.SERVICE_UNAVAILABLE, "DEPENDENCY_UNAVAILABLE",
        ex.getDependency() + " is unavailable: " + ex.getMessage()
            + " — the request was not applied and can be retried once the dependency recovers",
        request);
  }

  /** An upstream answered, but unusably. The fault is behind us, not with the caller. */
  @ExceptionHandler(BadGatewayException.class)
  public ResponseEntity<ApiError> handleBadGateway(
      BadGatewayException ex, HttpServletRequest request) {
    org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class)
        .warn("event=BAD_GATEWAY dependency={} upstreamStatus={} path={}",
            ex.getDependency(), ex.getUpstreamStatus(), request.getRequestURI());
    return error(HttpStatus.BAD_GATEWAY, "UPSTREAM_ERROR",
        ex.getDependency() + " returned an unusable response (HTTP " + ex.getUpstreamStatus() + "): "
            + ex.getMessage(), request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> handleBadRequest(
      IllegalArgumentException ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request);
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiError> handleIllegalState(
      IllegalStateException ex, HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, "ILLEGAL_STATE", ex.getMessage(), request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleUnreadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    String detail = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
    return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
        "Malformed request body: " + detail, request);
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleConflict(
      org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest request) {
    return error(HttpStatus.CONFLICT, "CONSTRAINT_VIOLATION",
        "The request conflicts with an existing record (duplicate key or foreign key)",
        request);
  }

  /**
   * Nothing should reach here. The message is generic on purpose and the real
   * cause goes to the log with the correlation ID attached, so a caller can
   * quote the ID and an operator can find the stack trace.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
    String cid = MDC.get(CORRELATION_MDC_KEY);
    org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class)
        .error("event=UNHANDLED_EXCEPTION correlationId={} path={}", cid, request.getRequestURI(), ex);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
        "Unexpected error. Quote correlation ID " + (cid == null ? "n/a" : cid) + " when reporting this.",
        request);
  }

  private ResponseEntity<ApiError> error(
      HttpStatus status, String code, String message, HttpServletRequest request) {
    return ResponseEntity.status(status)
        .body(new ApiError(Instant.now(), status.value(), code, message,
            request.getRequestURI(), MDC.get(CORRELATION_MDC_KEY)));
  }
}
