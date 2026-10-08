package com.telecom.order.integration;

import com.telecom.order.integration.dto.CustomerProfile;
import com.telecom.order.integration.dto.CustomerValidationResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/** Synchronous REST client for customer-service (order → customer). */
@Component
public class CustomerServiceClient {

  private static final Logger log = LoggerFactory.getLogger(CustomerServiceClient.class);

  private final WebClient webClient;

  public CustomerServiceClient(WebClient customerWebClient) {
    this.webClient = customerWebClient;
  }

  /**
   * Calls POST /api/customers/{id}/validate. Never throws for business outcomes:
   * unknown customers and unreachable service map to {@code valid=false} reasons
   * so the order fails gracefully with evidence instead of a 500.
   */
  public CustomerValidationResult validateCustomer(Long customerId) {
    try {
      CustomerValidationResult res = webClient.post()
          .uri("/api/customers/{id}/validate", customerId)
          .retrieve()
          .bodyToMono(CustomerValidationResult.class)
          .block();
      log.info("event=CUSTOMER_VALIDATE_CALL customerId={} valid={}",
          customerId, res != null && res.valid());
      return res;
    } catch (WebClientResponseException.NotFound e) {
      log.warn("event=CUSTOMER_VALIDATE_CALL customerId={} outcome=CUSTOMER_NOT_FOUND", customerId);
      return new CustomerValidationResult(customerId, null, "UNKNOWN", false, List.of("CUSTOMER_NOT_FOUND"));
    } catch (WebClientException e) {
      log.warn("event=CUSTOMER_VALIDATE_CALL customerId={} outcome=UNAVAILABLE error={}", customerId, e.getMessage());
      return new CustomerValidationResult(customerId, null, "UNKNOWN", false,
          List.of("CUSTOMER_SERVICE_UNAVAILABLE"));
    }
  }

  /** Fetches the customer profile (for the notification recipient). Returns null when unavailable. */
  public CustomerProfile getCustomer(Long customerId) {
    try {
      return webClient.get()
          .uri("/api/customers/{id}", customerId)
          .retrieve()
          .bodyToMono(CustomerProfile.class)
          .block();
    } catch (WebClientException e) {
      log.warn("event=CUSTOMER_GET_CALL customerId={} outcome=UNAVAILABLE error={}", customerId, e.getMessage());
      return null;
    }
  }
}
