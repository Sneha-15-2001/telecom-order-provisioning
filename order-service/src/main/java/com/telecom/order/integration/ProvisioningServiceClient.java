package com.telecom.order.integration;

import com.telecom.order.integration.dto.ProvisioningCreateRequest;
import com.telecom.order.integration.dto.ProvisioningRequestResult;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/** Synchronous REST client for provisioning-service (order → provisioning). Used by fulfillment in Phase 10. */
@Component
public class ProvisioningServiceClient {

  private static final Logger log = LoggerFactory.getLogger(ProvisioningServiceClient.class);

  private final WebClient webClient;

  public ProvisioningServiceClient(WebClient provisioningWebClient) {
    this.webClient = provisioningWebClient;
  }

  public ProvisioningRequestResult create(ProvisioningCreateRequest req) {
    ProvisioningRequestResult res = webClient.post()
        .uri("/api/provisioning")
        .bodyValue(req)
        .retrieve()
        .bodyToMono(ProvisioningRequestResult.class)
        .block();
    log.info("event=PROVISIONING_CREATE_CALL orderId={} request={}",
        req.orderId(), res != null ? res.requestNumber() : null);
    return res;
  }

  public ProvisioningRequestResult start(Long requestId) {
    return webClient.post()
        .uri("/api/provisioning/{id}/start", requestId)
        .retrieve()
        .bodyToMono(ProvisioningRequestResult.class)
        .block();
  }

  public ProvisioningRequestResult activate(Long requestId) {
    return webClient.post()
        .uri("/api/provisioning/{id}/activate", requestId)
        .retrieve()
        .bodyToMono(ProvisioningRequestResult.class)
        .block();
  }

  public ProvisioningRequestResult rollback(Long requestId) {
    return webClient.post()
        .uri("/api/provisioning/{id}/rollback", requestId)
        .retrieve()
        .bodyToMono(ProvisioningRequestResult.class)
        .block();
  }

  /**
   * Provisioning requests raised for an order.
   *
   * rollback() arrives in a later request than create(), so it has no handle on
   * the request it must reverse — this lookup is what lets compensation find it.
   */
  public List<ProvisioningRequestResult> requestsForOrder(Long orderId) {
    try {
      ProvisioningRequestResult[] arr = webClient.get()
          .uri("/api/provisioning/order/{orderId}", orderId)
          .retrieve()
          .bodyToMono(ProvisioningRequestResult[].class)
          .block();
      return arr == null ? List.of() : List.of(arr);
    } catch (RuntimeException e) {
      log.warn("event=PROVISIONING_LOOKUP_FAILED orderId={} error={}", orderId, e.toString());
      return List.of();
    }
  }
}
