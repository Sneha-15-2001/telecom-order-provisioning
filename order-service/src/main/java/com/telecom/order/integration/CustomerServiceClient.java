package com.telecom.order.integration;

import com.telecom.order.integration.dto.CustomerProfile;
import com.telecom.order.exception.BadGatewayException;
import com.telecom.order.exception.DependencyUnavailableException;
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
   * Calls POST /api/customers/{id}/validate.
   *
   * <p>A customer that is genuinely not eligible comes back as
   * {@code valid=false} with reasons, and the order fails as a business
   * decision. A customer-service that is down, timing out, or erroring is a
   * completely different thing: nobody has answered, so the caller must be told
   * 503 and the order left retryable. Folding the second case into the first
   * meant a CRM outage permanently failed customer orders.
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
    } catch (WebClientResponseException e) {
      // Upstream answered with a server error: the request cannot be judged.
      log.warn("event=CUSTOMER_VALIDATE_CALL customerId={} outcome=UPSTREAM_ERROR status={}",
          customerId, e.getRawStatusCode());
      throw new BadGatewayException("customer-service", e.getRawStatusCode(),
          "customer validation could not be completed", e);
    } catch (WebClientException e) {
      log.warn("event=CUSTOMER_VALIDATE_CALL customerId={} outcome=UNAVAILABLE error={}", customerId, e.getMessage());
      throw new DependencyUnavailableException("customer-service",
          "could not be reached to validate the customer", e);
    }
  }

  /**
   * Checks whether the customer may take on a new service.
   *
   * <p>Distinct from {@link #validateCustomer}: that asks "is this account in
   * good standing?", this asks "does this account already have a live service,
   * and is the account open?". An upgrade or plan change is meaningless without
   * an existing subscription, and this is where customer-service answers that.
   *
   * @return null when customer-service cannot be reached — the caller decides
   *     whether to proceed or block.
   */
  public CustomerValidationResult eligibility(Long customerId) {
    try {
      CustomerValidationResult res = webClient.get()
          .uri("/api/customers/{id}/eligibility", customerId)
          .retrieve()
          .bodyToMono(CustomerValidationResult.class)
          .block();
      log.info("event=CUSTOMER_ELIGIBILITY_CALL customerId={} eligible={}",
          customerId, res != null && res.valid());
      return res;
    } catch (WebClientException e) {
      log.warn("event=CUSTOMER_ELIGIBILITY_CALL customerId={} outcome=UNAVAILABLE error={}",
          customerId, e.getMessage());
      return null;
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
