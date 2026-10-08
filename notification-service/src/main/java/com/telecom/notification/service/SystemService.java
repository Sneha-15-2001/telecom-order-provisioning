package com.telecom.notification.service;

import com.telecom.notification.config.CorrelationIdFilter;
import com.telecom.notification.dto.SystemInfoResponse;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Phase 1 foundation service. Simulated SMS/email notifications, templates
 * and retry arrive in Phase 6.
 */
@Service
public class SystemService {

  private static final Logger log = LoggerFactory.getLogger(SystemService.class);

  private final String serviceName;
  private final String version;
  private final int port;

  public SystemService(
      @Value("${spring.application.name:notification-service}") String serviceName,
      @Value("${app.version:0.0.1-SNAPSHOT}") String version,
      @Value("${server.port:8085}") int port) {
    this.serviceName = serviceName;
    this.version = version;
    this.port = port;
  }

  public Map<String, String> ping() {
    log.info(
        "service={} correlationId={} event=SYSTEM_PING status=SUCCESS",
        serviceName, MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    return Map.of("status", "UP", "service", serviceName);
  }

  public SystemInfoResponse info() {
    log.info(
        "service={} correlationId={} event=SYSTEM_INFO status=SUCCESS",
        serviceName, MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
    return new SystemInfoResponse(
        serviceName,
        version,
        port,
        Instant.now(),
        MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY));
  }
}
