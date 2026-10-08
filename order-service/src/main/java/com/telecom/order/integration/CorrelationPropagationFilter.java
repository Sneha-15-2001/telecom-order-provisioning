package com.telecom.order.integration;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

/**
 * Propagates {@code X-Correlation-ID} to every downstream REST call.
 *
 * <p>Reuses the ID from the logging MDC (set by {@code CorrelationIdFilter}
 * for the inbound request); generates one if absent so downstream services
 * and the future AI investigator (Phase 14) can always correlate.
 */
@Component
public class CorrelationPropagationFilter {

  public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

  private final ExchangeFilterFunction filter = (request, next) -> {
    String correlationId = MDC.get("correlationId");
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
      MDC.put("correlationId", correlationId);
    }
    ClientRequest withId = ClientRequest.from(request)
        .header(CORRELATION_ID_HEADER, correlationId)
        .build();
    return next.exchange(withId);
  };

  public ExchangeFilterFunction filter() {
    return filter;
  }
}
