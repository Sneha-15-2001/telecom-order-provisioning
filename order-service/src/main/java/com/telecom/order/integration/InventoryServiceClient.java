package com.telecom.order.integration;

import com.telecom.order.exception.BadGatewayException;
import com.telecom.order.exception.DependencyUnavailableException;
import com.telecom.order.integration.dto.InventoryResourceItem;
import com.telecom.order.integration.dto.InventoryReservationResult;
import com.telecom.order.integration.dto.InventoryReserveRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClient;

/** Synchronous REST client for inventory-service (order → inventory). Used by fulfillment in Phase 10. */
@Component
public class InventoryServiceClient {

  private static final Logger log = LoggerFactory.getLogger(InventoryServiceClient.class);

  private final WebClient webClient;

  public InventoryServiceClient(WebClient inventoryWebClient) {
    this.webClient = inventoryWebClient;
  }

  public InventoryReservationResult reserve(InventoryReserveRequest req) {
    InventoryReservationResult res = webClient.post()
        .uri("/api/inventory/reserve")
        .bodyValue(req)
        .retrieve()
        .bodyToMono(InventoryReservationResult.class)
        .block();
    log.info("event=INVENTORY_RESERVE_CALL orderId={} reservation={}",
        req.orderId(), res != null ? res.reservationNumber() : null);
    return res;
  }

  public InventoryReservationResult confirm(Long reservationId) {
    return webClient.post()
        .uri("/api/inventory/reservations/{id}/confirm", reservationId)
        .retrieve()
        .bodyToMono(InventoryReservationResult.class)
        .block();
  }

  public InventoryReservationResult cancel(Long reservationId) {
    return webClient.post()
        .uri("/api/inventory/reservations/{id}/cancel", reservationId)
        .retrieve()
        .bodyToMono(InventoryReservationResult.class)
        .block();
  }

  /** First free resource of a type (for fulfillment auto-allocation). */
  public List<InventoryResourceItem> firstAvailable(String resourceType) {
    return fetchAvailable(resourceType);
  }

  /**
   * Allocation lookup that refuses to invent an empty result.
   *
   * <p>If inventory-service is down, the old code let the WebClient exception
   * propagate and every call site turned it into "no stock available", which is
   * indistinguishable from a genuine shortage. Fulfillment then parked the
   * order in INVENTORY_SHORTAGE — blaming the stockroom for an outage. The
   * caller now learns the dependency failed instead.
   */
  private List<InventoryResourceItem> fetchAvailable(String resourceType) {
    InventoryResourceItem[] arr;
    try {
      arr = webClient.get()
          .uri(uriBuilder -> uriBuilder.path("/api/inventory/available")
              .queryParam("type", resourceType)
              .queryParam("limit", 1)
              .build())
          .retrieve()
          .bodyToMono(InventoryResourceItem[].class)
          .block();
    } catch (WebClientResponseException e) {
      throw new BadGatewayException("inventory-service", e.getRawStatusCode(),
          "stock lookup returned an unusable response", e);
    } catch (WebClientException e) {
      throw new DependencyUnavailableException("inventory-service",
          "could not be reached to check stock", e);
    }
    return arr == null ? List.of() : List.of(arr);
  }

  /**
   * Every reservation raised for an order.
   *
   * Compensation needs this: rollback() runs in a separate request from
   * fulfill(), so it has no in-memory handle on the reservation it must release.
   * Without the lookup, a rollback leaves the number or SIM held forever.
   */
  public List<InventoryReservationResult> reservationsForOrder(Long orderId) {
    try {
      InventoryReservationResult[] arr = webClient.get()
          .uri("/api/inventory/reservations/order/{orderId}", orderId)
          .retrieve()
          .bodyToMono(InventoryReservationResult[].class)
          .block();
      return arr == null ? List.of() : List.of(arr);
    } catch (RuntimeException e) {
      log.warn("event=INVENTORY_RESERVATIONS_LOOKUP_FAILED orderId={} error={}", orderId, e.toString());
      return List.of();
    }
  }
}
