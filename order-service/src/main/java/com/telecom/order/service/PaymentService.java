package com.telecom.order.service;

import com.telecom.order.config.CorrelationIdFilter;
import com.telecom.order.dto.PaymentResponse;
import com.telecom.order.dto.PaymentValidationResponse;
import com.telecom.order.dto.RecordPaymentRequest;
import com.telecom.order.entity.CustomerOrder;
import com.telecom.order.entity.OrderHistory;
import com.telecom.order.entity.OrderStatus;
import com.telecom.order.entity.Payment;
import com.telecom.order.entity.PaymentStatus;
import com.telecom.order.mapper.OrderMapper;
import com.telecom.order.repository.OrderHistoryRepository;
import com.telecom.order.repository.OrderItemRepository;
import com.telecom.order.repository.OrderRepository;
import com.telecom.order.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment attempts against PAYMENT_PENDING orders plus simulated gateway validation.
 * Real payment-provider integration is out of scope; validation compares the paid
 * amount with the order payable total (total − discount).
 */
@Service
public class PaymentService {

  private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

  private final PaymentRepository payments;
  private final OrderRepository orders;
  private final OrderItemRepository items;
  private final OrderHistoryRepository history;

  public PaymentService(PaymentRepository payments, OrderRepository orders,
      OrderItemRepository items, OrderHistoryRepository history) {
    this.payments = payments;
    this.orders = orders;
    this.items = items;
    this.history = history;
  }

  @Transactional
  public PaymentResponse record(Long orderId, RecordPaymentRequest req) {
    CustomerOrder o = getOrder(orderId);
    if (o.getStatus() != OrderStatus.PAYMENT_PENDING) {
      throw new IllegalArgumentException(
          "Payments can be recorded only for PAYMENT_PENDING orders (current: " + o.getStatus() + ")");
    }
    Payment p = new Payment();
    p.setOrder(o);
    p.setPaymentReference("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    p.setAmount(req.amount());
    p.setMethod(req.method());
    p.setStatus(PaymentStatus.PENDING);
    Payment saved = payments.save(p);
    record(o, "PAYMENT_RECORDED", "attempt " + saved.getPaymentReference() + " " + req.amount() + " via " + req.method());
    log.info("service=order-service correlationId={} orderId={} event=PAYMENT_RECORDED status=PENDING",
        correlationId(), orderId);
    return OrderMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<PaymentResponse> list(Long orderId) {
    getOrder(orderId);
    return payments.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
        .map(OrderMapper::toResponse).toList();
  }

  @Transactional
  public PaymentValidationResponse validate(Long orderId) {
    CustomerOrder o = getOrder(orderId);
    Payment latest = payments.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("No payment recorded for order: " + orderId));
    BigDecimal expected = o.getTotalAmount().subtract(o.getDiscountAmount()).max(BigDecimal.ZERO);
    boolean ok = latest.getAmount().compareTo(expected) >= 0
        && o.getStatus() == OrderStatus.PAYMENT_PENDING;
    if (ok) {
      latest.setStatus(PaymentStatus.SUCCESS);
      OrderStatus from = o.getStatus();
      o.setStatus(OrderStatus.PAYMENT_COMPLETED);
      record(o, "PAYMENT_VALIDATED", "gateway approved " + latest.getPaymentReference());
      recordStatus(o, from, OrderStatus.PAYMENT_COMPLETED, "payment completed");
    } else {
      latest.setStatus(PaymentStatus.FAILED);
      OrderStatus from = o.getStatus();
      o.setStatus(OrderStatus.FAILED);
      record(o, "PAYMENT_VALIDATED", "gateway declined: paid " + latest.getAmount() + " expected " + expected);
      recordStatus(o, from, OrderStatus.FAILED, "payment validation failed");
    }
    log.info("service=order-service correlationId={} orderId={} event=PAYMENT_VALIDATED valid={} status={}",
        correlationId(), orderId, ok, o.getStatus());
    return new PaymentValidationResponse(o.getId(), latest.getPaymentReference(),
        expected, latest.getAmount(), ok, o.getStatus());
  }

  private CustomerOrder getOrder(Long id) {
    return orders.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
  }

  private void record(CustomerOrder o, String event, String comment) {
    OrderHistory h = new OrderHistory();
    h.setOrder(o);
    h.setEvent(event);
    h.setFromStatus(o.getStatus());
    h.setToStatus(o.getStatus());
    h.setComment(comment);
    history.save(h);
  }

  private void recordStatus(CustomerOrder o, OrderStatus from, OrderStatus to, String comment) {
    OrderHistory h = new OrderHistory();
    h.setOrder(o);
    h.setEvent("STATUS_CHANGED");
    h.setFromStatus(from);
    h.setToStatus(to);
    h.setComment(comment);
    history.save(h);
  }

  private String correlationId() {
    return MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
  }
}
