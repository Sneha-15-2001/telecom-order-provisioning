package com.telecom.order.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.order.dto.CreateOrderRequest;
import com.telecom.order.dto.OrderItemRequest;
import com.telecom.order.dto.OrderResponse;
import com.telecom.order.dto.OrderStatusResponse;
import com.telecom.order.entity.OrderItemType;
import com.telecom.order.entity.OrderPriority;
import com.telecom.order.entity.OrderStatus;
import com.telecom.order.entity.OrderType;
import com.telecom.order.service.OrderService;
import com.telecom.order.service.PaymentService;
import com.telecom.order.service.PromotionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({OrderController.class, PromotionController.class})
class OrderControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean OrderService orderService;
  @MockBean PaymentService paymentService;
  @MockBean PromotionService promotionService;

  private OrderResponse sample() {
    return new OrderResponse(1L, "ORD-ABC123", 1L, "CUS-1", OrderType.NEW_CONNECTION,
        OrderStatus.CREATED, OrderPriority.NORMAL, new BigDecimal("299.00"),
        BigDecimal.ZERO, new BigDecimal("299.00"), null, null, List.of(),
        LocalDateTime.now(), LocalDateTime.now());
  }

  @Test
  void createReturns201() throws Exception {
    when(orderService.create(any())).thenReturn(sample());
    var req = new CreateOrderRequest(1L, "CUS-1", OrderType.NEW_CONNECTION, null, null,
        List.of(new OrderItemRequest(OrderItemType.MOBILE_PLAN, "PLAN_5G_299", "5G 299",
            1, new BigDecimal("299.00"), null)));

    mvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.orderNumber").value("ORD-ABC123"));
  }

  @Test
  void createRejectsEmptyItemsWith400() throws Exception {
    var req = new CreateOrderRequest(1L, "CUS-1", OrderType.NEW_CONNECTION, null, null, List.of());

    mvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void statusEndpointReturnsContract() throws Exception {
    when(orderService.status(eq(1L)))
        .thenReturn(new OrderStatusResponse(1L, "ORD-ABC123", OrderStatus.VALIDATED));

    mvc.perform(get("/api/orders/1/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("VALIDATED"));
  }
}
