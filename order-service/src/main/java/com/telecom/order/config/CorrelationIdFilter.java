package com.telecom.order.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Guarantees every request carries an {@code X-Correlation-ID}.
 *
 * <p>If the caller supplies one it is reused, otherwise a UUID is generated.
 * The ID is stored in the SLF4J MDC under {@code correlationId} (so every log
 * line can be traced), echoed back as a response header, and exposed as a
 * request attribute for controllers/services.
 *
 * <p>Downstream propagation to other microservices (REST clients forwarding
 * the header) is implemented in Phase 7. The future AI investigator
 * (Phase 14) relies on this ID to correlate logs across services.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  public static final String CORRELATION_ID_MDC_KEY = "correlationId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }
    MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
    request.setAttribute(CORRELATION_ID_HEADER, correlationId);
    response.setHeader(CORRELATION_ID_HEADER, correlationId);
    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(CORRELATION_ID_MDC_KEY);
    }
  }
}
