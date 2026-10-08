package com.telecom.order.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import com.telecom.order.integration.dto.InventoryReserveRequest;
import com.telecom.order.integration.dto.NotificationNotifyRequest;
import com.telecom.order.integration.dto.ProvisioningCreateRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Proves Phase 7 REST wiring without any real downstream service: a JDK
 * HttpServer stub captures the {@code X-Correlation-ID} header on every call
 * and returns canned payloads for all four clients.
 */
class IntegrationClientTest {

  private HttpServer stub;
  private String baseUrl;
  private final Map<String, String> capturedCorrelationIds = new ConcurrentHashMap<>();

  @BeforeEach
  void startStub() throws IOException {
    stub = HttpServer.create(new InetSocketAddress(0), 0);
    stub.createContext("/", exchange -> {
      String path = exchange.getRequestURI().getPath();
      capturedCorrelationIds.put(path, exchange.getRequestHeaders().getFirst("X-Correlation-ID"));
      String body = route(path);
      int code = body == null ? 404 : 200;
      byte[] bytes = (body == null ? "{\"error\":\"not found\"}" : body).getBytes(StandardCharsets.UTF_8);
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(code, bytes.length);
      try (OutputStream os = exchange.getResponseBody()) {
        os.write(bytes);
      }
    });
    stub.start();
    baseUrl = "http://localhost:" + stub.getAddress().getPort();
    MDC.put("correlationId", "TEST-CORR-1");
  }

  @AfterEach
  void stopStub() {
    stub.stop(0);
    MDC.clear();
  }

  private String route(String path) {
    return switch (path) {
      case "/api/customers/7/validate" ->
          "{\"customerId\":7,\"customerNumber\":\"CUS-7\",\"status\":\"ACTIVE\",\"valid\":true,\"reasons\":[]}";
      case "/api/inventory/reserve" ->
          "{\"id\":11,\"reservationNumber\":\"RSV-TEST\",\"resourceId\":1,\"resourceNumber\":\"RES-1\","
              + "\"orderId\":2,\"customerId\":1,\"status\":\"ACTIVE\"}";
      case "/api/inventory/reservations/11/confirm", "/api/inventory/reservations/12/cancel" ->
          "{\"id\":11,\"reservationNumber\":\"RSV-TEST\",\"resourceId\":1,\"resourceNumber\":\"RES-1\","
              + "\"orderId\":2,\"customerId\":1,\"status\":\"CONFIRMED\"}";
      case "/api/provisioning" ->
          "{\"id\":21,\"requestNumber\":\"PRV-TEST\",\"orderId\":2,\"serviceType\":\"MOBILE\",\"status\":\"PENDING\",\"lastError\":null}";
      case "/api/provisioning/21/start" ->
          "{\"id\":21,\"requestNumber\":\"PRV-TEST\",\"orderId\":2,\"serviceType\":\"MOBILE\",\"status\":\"IN_PROGRESS\",\"lastError\":null}";
      case "/api/provisioning/21/activate" ->
          "{\"id\":21,\"requestNumber\":\"PRV-TEST\",\"orderId\":2,\"serviceType\":\"MOBILE\",\"status\":\"COMPLETED\",\"lastError\":null}";
      case "/api/notifications/notify" ->
          "{\"id\":31,\"notificationNumber\":\"NTF-TEST\",\"status\":\"PENDING\"}";
      case "/api/inventory/available" ->
          "[{\"id\":5,\"resourceNumber\":\"RES-MSISDN05\",\"identifier\":\"919000000005\",\"status\":\"AVAILABLE\"}]";
      case "/api/customers/7" ->
          "{\"id\":7,\"customerNumber\":\"CUS-7\",\"firstName\":\"A\",\"lastName\":\"B\",\"email\":\"a@x.com\",\"phone\":\"+911111111111\"}";
      case "/api/provisioning/21/rollback" ->
          "{\"id\":21,\"requestNumber\":\"PRV-TEST\",\"orderId\":2,\"serviceType\":\"MOBILE\",\"status\":\"ROLLED_BACK\",\"lastError\":null}";
      default -> null;
    };
  }

  private WebClient webClient() {
    return WebClient.builder()
        .baseUrl(baseUrl)
        .filter(new CorrelationPropagationFilter().filter())
        .build();
  }

  @Test
  void customerValidatePropagatesCorrelationId() {
    var client = new CustomerServiceClient(webClient());
    var res = client.validateCustomer(7L);
    assertThat(res.valid()).isTrue();
    assertThat(res.customerNumber()).isEqualTo("CUS-7");
    assertThat(capturedCorrelationIds.get("/api/customers/7/validate")).isEqualTo("TEST-CORR-1");
  }

  @Test
  void customerNotFoundMapsToInvalid() {
    var client = new CustomerServiceClient(webClient());
    var res = client.validateCustomer(999L);
    assertThat(res.valid()).isFalse();
    assertThat(res.reasons()).contains("CUSTOMER_NOT_FOUND");
  }

  @Test
  void inventoryClientPropagatesCorrelationId() {
    var client = new InventoryServiceClient(webClient());
    var res = client.reserve(new InventoryReserveRequest(1L, 2L, 1L, 30));
    assertThat(res.reservationNumber()).isEqualTo("RSV-TEST");
    assertThat(capturedCorrelationIds.get("/api/inventory/reserve")).isEqualTo("TEST-CORR-1");
    assertThat(client.confirm(11L).status()).isEqualTo("CONFIRMED");
    assertThat(client.cancel(12L).status()).isEqualTo("CONFIRMED");
  }

  @Test
  void provisioningClientPropagatesCorrelationId() {
    var client = new ProvisioningServiceClient(webClient());
    var created = client.create(new ProvisioningCreateRequest(2L, 1L, "MOBILE", "919876543210", null, "PLAN_5G_299"));
    assertThat(created.requestNumber()).isEqualTo("PRV-TEST");
    assertThat(capturedCorrelationIds.get("/api/provisioning")).isEqualTo("TEST-CORR-1");
    assertThat(client.start(21L).status()).isEqualTo("IN_PROGRESS");
    assertThat(client.activate(21L).status()).isEqualTo("COMPLETED");
  }

  @Test
  void notificationClientPropagatesCorrelationId() {
    var client = new NotificationServiceClient(webClient());
    var res = client.notify(new NotificationNotifyRequest(2L, 1L, "SMS", "+919876543210",
        "ORDER_CREATED", Map.of("orderNumber", "ORD-1")));
    assertThat(res.notificationNumber()).isEqualTo("NTF-TEST");
    assertThat(capturedCorrelationIds.get("/api/notifications/notify")).isEqualTo("TEST-CORR-1");
  }

  @Test
  void inventoryAvailableAndCustomerProfile() {
    var inventory = new InventoryServiceClient(webClient());
    var free = inventory.firstAvailable("MSISDN");
    assertThat(free).hasSize(1);
    assertThat(free.get(0).identifier()).isEqualTo("919000000005");
    assertThat(capturedCorrelationIds.get("/api/inventory/available")).isEqualTo("TEST-CORR-1");

    var customer = new CustomerServiceClient(webClient());
    assertThat(customer.getCustomer(7L).phone()).isEqualTo("+911111111111");
    assertThat(customer.getCustomer(999L)).isNull();

    var provisioning = new ProvisioningServiceClient(webClient());
    assertThat(provisioning.rollback(21L).status()).isEqualTo("ROLLED_BACK");
  }
}
