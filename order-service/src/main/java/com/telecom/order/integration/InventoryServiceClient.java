package com.telecom.order.integration;

import com.telecom.order.integration.dto.InventoryResourceItem;
import com.telecom.order.integration.dto.InventoryReservationResult;
import com.telecom.order.integration.dto.InventoryReserveRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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
    InventoryResourceItem[] arr = webClient.get()
        .uri(uriBuilder -> uriBuilder.path("/api/inventory/available")
            .queryParam("type", resourceType)
            .queryParam("limit", 1)
            .build())
        .retrieve()
        .bodyToMono(InventoryResourceItem[].class)
        .block();
    return arr == null ? List.of() : List.of(arr);
  }
}
