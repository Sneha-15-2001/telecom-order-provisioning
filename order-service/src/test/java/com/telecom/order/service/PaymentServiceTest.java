package com.telecom.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.telecom.order.dto.PaymentValidationResponse;
import com.telecom.order.dto.RecordPaymentRequest;
import com.telecom.order.entity.CustomerOrder;
import com.telecom.order.entity.OrderStatus;
import com.telecom.order.entity.Payment;
import com.telecom.order.entity.PaymentMethod;
import com.telecom.order.entity.PaymentStatus;
import com.telecom.order.repository.OrderHistoryRepository;
import com.telecom.order.repository.OrderItemRepository;
import com.telecom.order.repository.OrderRepository;
import com.telecom.order.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

  @Mock PaymentRepository payments;
  @Mock OrderRepository orders;
  @Mock OrderItemRepository items;
  @Mock OrderHistoryRepository history;

  @InjectMocks PaymentService service;

  private CustomerOrder order(OrderStatus status, String total, String discount) {
    CustomerOrder o = new CustomerOrder();
    ReflectionTestUtils.setField(o, "id", 40L);
    o.setStatus(status);
    o.setTotalAmount(new BigDecimal(total));
    o.setDiscountAmount(new BigDecimal(discount));
    return o;
  }

  private Payment payment(String amount, PaymentStatus status) {
    Payment p = new Payment();
    p.setAmount(new BigDecimal(amount));
    p.setMethod(PaymentMethod.UPI);
    p.setStatus(status);
    p.setPaymentReference("PAY-T1");
    return p;
  }

  @Test
  void recordRequiresPaymentPending() {
    when(orders.findById(40L)).thenReturn(Optional.of(order(OrderStatus.VALIDATED, "100.00", "0")));

    assertThatThrownBy(() -> service.record(40L, new RecordPaymentRequest(new BigDecimal("100.00"), PaymentMethod.UPI)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("PAYMENT_PENDING");
  }

  @Test
  void validateApprovesExactAmount() {
    CustomerOrder o = order(OrderStatus.PAYMENT_PENDING, "299.00", "29.90");
    when(orders.findById(40L)).thenReturn(Optional.of(o));
    when(payments.findByOrderIdOrderByCreatedAtDesc(40L))
        .thenReturn(List.of(payment("269.10", PaymentStatus.PENDING)));

    PaymentValidationResponse res = service.validate(40L);
    assertThat(res.valid()).isTrue();
    assertThat(res.orderStatus()).isEqualTo(OrderStatus.PAYMENT_COMPLETED);
  }

  @Test
  void validateFailsShortPayment() {
    CustomerOrder o = order(OrderStatus.PAYMENT_PENDING, "299.00", "0");
    when(orders.findById(40L)).thenReturn(Optional.of(o));
    when(payments.findByOrderIdOrderByCreatedAtDesc(40L))
        .thenReturn(List.of(payment("100.00", PaymentStatus.PENDING)));

    PaymentValidationResponse res = service.validate(40L);
    assertThat(res.valid()).isFalse();
    assertThat(res.orderStatus()).isEqualTo(OrderStatus.FAILED);
  }

  @Test
  void validateWithoutPaymentThrows404() {
    when(orders.findById(40L)).thenReturn(Optional.of(order(OrderStatus.PAYMENT_PENDING, "10.00", "0")));
    when(payments.findByOrderIdOrderByCreatedAtDesc(40L)).thenReturn(List.of());

    assertThatThrownBy(() -> service.validate(40L)).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void listPayments() {
    when(orders.findById(40L)).thenReturn(Optional.of(order(OrderStatus.PAYMENT_PENDING, "10.00", "0")));
    when(payments.findByOrderIdOrderByCreatedAtDesc(40L)).thenReturn(List.of());
    assertThat(service.list(40L)).isEmpty();
  }
}
