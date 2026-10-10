package com.telecom.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.telecom.order.dto.ApplyPromotionRequest;
import com.telecom.order.dto.BulkCreateOrderRequest;
import com.telecom.order.dto.BulkOrderItemResult;
import com.telecom.order.dto.CreateOrderRequest;
import com.telecom.order.dto.OrderItemRequest;
import com.telecom.order.dto.OrderResponse;
import com.telecom.order.entity.CustomerOrder;
import com.telecom.order.entity.DiscountType;
import com.telecom.order.entity.OrderItem;
import com.telecom.order.entity.OrderItemType;
import com.telecom.order.entity.OrderStatus;
import com.telecom.order.entity.OrderType;
import com.telecom.order.entity.Promotion;
import com.telecom.order.integration.CustomerServiceClient;
import com.telecom.order.integration.InventoryServiceClient;
import com.telecom.order.integration.NotificationServiceClient;
import com.telecom.order.integration.ProvisioningServiceClient;
import com.telecom.order.integration.dto.CustomerValidationResult;
import com.telecom.order.repository.OrderHistoryRepository;
import com.telecom.order.repository.OrderItemRepository;
import com.telecom.order.repository.OrderRepository;
import com.telecom.order.repository.PaymentRepository;
import com.telecom.order.repository.PromotionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

  @Mock OrderRepository orders;
  @Mock OrderItemRepository items;
  @Mock OrderHistoryRepository history;
  @Mock PaymentRepository payments;
  @Mock PromotionRepository promotions;
  @Mock CustomerServiceClient customerClient;
  @Mock InventoryServiceClient inventoryClient;
  @Mock ProvisioningServiceClient provisioningClient;
  @Mock NotificationServiceClient notificationClient;

  @InjectMocks OrderService service;

  private CreateOrderRequest createReq() {
    return new CreateOrderRequest(1L, "CUS-1", OrderType.NEW_CONNECTION, null, null,
        List.of(new OrderItemRequest(OrderItemType.MOBILE_PLAN, "PLAN_5G_299", "5G 299",
            2, new BigDecimal("299.00"), null)));
  }

  @Test
  void createComputesTotalFromItems() {
    when(orders.save(any(CustomerOrder.class))).thenAnswer(i -> i.getArgument(0));
    OrderItem saved = new OrderItem();
    saved.setQuantity(2);
    saved.setUnitPrice(new BigDecimal("299.00"));
    when(items.findByOrderIdOrderByIdAsc(any())).thenReturn(List.of(saved));

    OrderResponse res = service.create(createReq());
    assertThat(res.orderNumber()).startsWith("ORD-");
    assertThat(res.status()).isEqualTo(OrderStatus.CREATED);
    assertThat(res.totalAmount()).isEqualByComparingTo("598.00");
  }

  @Test
  void submitRequiresValidated() {
    CustomerOrder o = new CustomerOrder();
    o.setStatus(OrderStatus.CREATED);
    when(orders.findById(1L)).thenReturn(Optional.of(o));

    assertThatThrownBy(() -> service.submit(1L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("VALIDATED");
  }

  @Test
  void promoDoubleApplyRejected() {
    CustomerOrder o = new CustomerOrder();
    o.setStatus(OrderStatus.VALIDATED);
    o.setTotalAmount(new BigDecimal("299.00"));
    o.setPromoCode("FESTIVE10");
    when(orders.findById(2L)).thenReturn(Optional.of(o));

    assertThatThrownBy(() -> service.applyPromotion(2L, new ApplyPromotionRequest("FESTIVE10")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already applied");
  }

  @Test
  void promoPercentageComputesDiscount() {
    CustomerOrder o = new CustomerOrder();
    o.setStatus(OrderStatus.CREATED);
    o.setTotalAmount(new BigDecimal("1000.00"));
    o.setDiscountAmount(BigDecimal.ZERO);
    when(orders.findById(3L)).thenReturn(Optional.of(o));
    Promotion p = new Promotion();
    p.setPromoCode("FESTIVE10");
    p.setDiscountType(DiscountType.PERCENTAGE);
    p.setDiscountValue(new BigDecimal("10"));
    p.setActive(true);
    when(promotions.findByPromoCode("FESTIVE10")).thenReturn(Optional.of(p));
    when(items.findByOrderIdOrderByIdAsc(any())).thenReturn(List.of());

    OrderResponse res = service.applyPromotion(3L, new ApplyPromotionRequest("FESTIVE10"));
    assertThat(res.discountAmount()).isEqualByComparingTo("100.00");
    assertThat(res.payableAmount()).isEqualByComparingTo("900.00");
  }

  @Test
  void rollbackAllowedFromFailed() {
    CustomerOrder o = new CustomerOrder();
    o.setStatus(OrderStatus.FAILED);
    o.setTotalAmount(new BigDecimal("299.00"));
    o.setDiscountAmount(BigDecimal.ZERO);
    when(orders.findById(4L)).thenReturn(Optional.of(o));
    when(items.findByOrderIdOrderByIdAsc(any())).thenReturn(List.of());

    assertThat(service.rollback(4L).status()).isEqualTo(OrderStatus.CANCELLED);
  }

  @Test
  void validateFailsOrderOnInvalidCustomer() {
    CustomerOrder o = new CustomerOrder();
    o.setStatus(OrderStatus.CREATED);
    o.setCustomerId(404L);
    o.setTotalAmount(new BigDecimal("10.00"));
    o.setDiscountAmount(BigDecimal.ZERO);
    when(orders.findById(5L)).thenReturn(Optional.of(o));
    OrderItem item = new OrderItem();
    item.setQuantity(1);
    item.setUnitPrice(new BigDecimal("10.00"));
    when(items.findByOrderIdOrderByIdAsc(5L)).thenReturn(List.of(item));
    when(customerClient.validateCustomer(404L)).thenReturn(
        new CustomerValidationResult(404L, null, "UNKNOWN", false, List.of("CUSTOMER_NOT_FOUND")));

    OrderResponse res = service.validate(5L);
    assertThat(res.status()).isEqualTo(OrderStatus.FAILED);
  }

  @Test
  void bulkReportsPartialFailurePerItem() {
    var realValidator = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator();
    var bulkService = new OrderService(orders, items, history, payments, promotions, realValidator, customerClient, null, null, null, false, 60);
    when(orders.save(any(CustomerOrder.class))).thenAnswer(i -> i.getArgument(0));
    OrderItem saved = new OrderItem();
    saved.setQuantity(1);
    saved.setUnitPrice(new BigDecimal("100.00"));
    when(items.findByOrderIdOrderByIdAsc(any())).thenReturn(List.of(saved));

    var bad = new CreateOrderRequest(null, null, OrderType.PLAN_CHANGE, null, null, List.of());
    List<BulkOrderItemResult> results = bulkService.bulkCreate(new BulkCreateOrderRequest(List.of(createReq(), bad)));

    assertThat(results).hasSize(2);
    assertThat(results.get(0).success()).isTrue();
    assertThat(results.get(0).orderNumber()).startsWith("ORD-");
    assertThat(results.get(1).success()).isFalse();
    assertThat(results.get(1).error()).contains("customerId");
  }

  private CustomerOrder paidOrder() {
    CustomerOrder o = new CustomerOrder();
    o.setOrderNumber("ORD-TEST20");
    o.setCustomerId(1L);
    o.setStatus(OrderStatus.PAYMENT_COMPLETED);
    o.setTotalAmount(new BigDecimal("299.00"));
    o.setDiscountAmount(BigDecimal.ZERO);
    return o;
  }

  private OrderItem planItem() {
    OrderItem i = new OrderItem();
    i.setItemType(OrderItemType.MOBILE_PLAN);
    i.setProductCode("PLAN_5G_299");
    i.setProductName("5G 299");
    i.setQuantity(1);
    i.setUnitPrice(new BigDecimal("299.00"));
    return i;
  }

  @Test
  void fulfillHappyPathCompletesOrder() {
    CustomerOrder o = paidOrder();
    when(orders.findById(20L)).thenReturn(Optional.of(o));
    when(items.findByOrderIdOrderByIdAsc(20L)).thenReturn(List.of(planItem()));
    when(inventoryClient.firstAvailable("MSISDN")).thenReturn(List.of(
        new com.telecom.order.integration.dto.InventoryResourceItem(5L, "RES-MSISDN05", "919000000005", "AVAILABLE")));
    when(inventoryClient.reserve(any())).thenReturn(
        new com.telecom.order.integration.dto.InventoryReservationResult(9L, "RSV-T1", 5L, "RES-MSISDN05", 20L, 1L, "ACTIVE"));
    when(inventoryClient.confirm(9L)).thenReturn(
        new com.telecom.order.integration.dto.InventoryReservationResult(9L, "RSV-T1", 5L, "RES-MSISDN05", 20L, 1L, "CONFIRMED"));
    when(provisioningClient.create(any())).thenReturn(
        new com.telecom.order.integration.dto.ProvisioningRequestResult(7L, "PRV-T1", 20L, "MOBILE", "PENDING", null));
    when(provisioningClient.start(7L)).thenReturn(
        new com.telecom.order.integration.dto.ProvisioningRequestResult(7L, "PRV-T1", 20L, "MOBILE", "IN_PROGRESS", null));
    when(provisioningClient.activate(7L)).thenReturn(
        new com.telecom.order.integration.dto.ProvisioningRequestResult(7L, "PRV-T1", 20L, "MOBILE", "COMPLETED", null));
    when(customerClient.getCustomer(1L)).thenReturn(
        new com.telecom.order.integration.dto.CustomerProfile(1L, "CUS-1", "A", "B", "a@x.com", "+911111111111"));

    var res = service.fulfill(20L, null);
    assertThat(res.orderStatus()).isEqualTo("COMPLETED");
    assertThat(res.steps()).allMatch(s -> "SUCCESS".equals(s.status()));
  }

  @Test
  void fulfillFailedActivationCompensates() {
    CustomerOrder o = paidOrder();
    when(orders.findById(21L)).thenReturn(Optional.of(o));
    when(items.findByOrderIdOrderByIdAsc(21L)).thenReturn(List.of(planItem()));
    when(inventoryClient.firstAvailable("MSISDN")).thenReturn(List.of(
        new com.telecom.order.integration.dto.InventoryResourceItem(6L, "RES-MSISDN06", "919000000006", "AVAILABLE")));
    when(inventoryClient.reserve(any())).thenReturn(
        new com.telecom.order.integration.dto.InventoryReservationResult(10L, "RSV-T2", 6L, "RES-MSISDN06", 21L, 1L, "ACTIVE"));
    when(provisioningClient.create(any())).thenReturn(
        new com.telecom.order.integration.dto.ProvisioningRequestResult(8L, "PRV-T2", 21L, "MOBILE", "PENDING", null));
    when(provisioningClient.start(8L)).thenReturn(
        new com.telecom.order.integration.dto.ProvisioningRequestResult(8L, "PRV-T2", 21L, "MOBILE", "IN_PROGRESS", null));
    when(provisioningClient.activate(8L)).thenReturn(
        new com.telecom.order.integration.dto.ProvisioningRequestResult(8L, "PRV-T2", 21L, "MOBILE", "FAILED", "MISSING_MSISDN"));

    var res = service.fulfill(21L, null);
    assertThat(res.orderStatus()).isEqualTo("FAILED");
    verify(inventoryClient).cancel(10L);
  }
}
