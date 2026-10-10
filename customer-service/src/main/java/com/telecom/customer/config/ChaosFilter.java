package com.telecom.customer.config;

import com.telecom.customer.config.ChaosSwitch.Fault;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Applies any fault armed on {@link ChaosSwitch} to matching requests.
 *
 * <p>Runs after the correlation filter so injected failures carry a traceable
 * ID like any other response. A no-op unless chaos is enabled.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ChaosFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(ChaosFilter.class);

  private final ChaosSwitch chaos;

  public ChaosFilter(ChaosSwitch chaos) {
    this.chaos = chaos;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    Fault fault = chaos.activeFault(path);
    if (fault == null) {
      chain.doFilter(request, response);
      return;
    }
    String cid = MDC.get("correlationId");
    log.warn("event=CHAOS_FAULT_INJECTED path={} status={} correlationId={}", path, fault.status(), cid);
    response.setStatus(fault.status());
    response.setContentType("application/json");
    String code = switch (fault.status()) {
      case 500 -> "INTERNAL_ERROR";
      case 502 -> "UPSTREAM_ERROR";
      case 503 -> "SERVICE_UNAVAILABLE";
      case 429 -> "RATE_LIMIT_EXCEEDED";
      default -> "INJECTED_FAULT";
    };
    response.getWriter().write(
        "{\"timestamp\":\"" + Instant.now() + "\","
            + "\"status\":" + fault.status() + ","
            + "\"error\":\"" + code + "\","
            + "\"message\":\"Injected fault for incident reproduction. Downstream callers must handle this.\","
            + "\"path\":\"" + path + "\","
            + "\"correlationId\":\"" + (cid == null ? "" : cid) + "\"}");
  }
}
