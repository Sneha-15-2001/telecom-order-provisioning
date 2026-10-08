package com.telecom.order.service;

import com.telecom.order.config.CorrelationIdFilter;
import com.telecom.order.dto.ApplyPromotionRequest;
import com.telecom.order.dto.BulkCreateOrderRequest;
import com.telecom.order.dto.BulkOrderItemResult;
import com.telecom.order.dto.CreateOrderRequest;
import com.telecom.order.dto.FulfillmentResponse;
import com.telecom.order.dto.FulfillmentResponse.FulfillmentStep;
import com.telecom.order.dto.ModifyOrderRequest;
import com.telecom.order.dto.OrderHistoryEntry;
import com.telecom.order.dto.OrderItemRequest;
import com.telecom.order.dto.OrderResponse;
import com.telecom.order.dto.OrderStatusResponse;
import com.telecom.order.dto.OrderTimelineResponse;
import com.telecom.order.dto.OrderTimelineResponse.TimelineEvent;
import com.telecom.order.dto.UpdateOrderRequest;
import com.telecom.order.entity.CustomerOrder;
import com.telecom.order.entity.DiscountType;
import com.telecom.order.entity.OrderHistory;
import com.telecom.order.entity.OrderItem;
import com.telecom.order.entity.OrderItemType;
import com.telecom.order.entity.OrderPriority;
import com.telecom.order.entity.OrderStatus;
import com.telecom.order.entity.Payment;
import com.telecom.order.entity.Promotion;
import com.telecom.order.integration.CustomerServiceClient;
import com.telecom.order.integration.InventoryServiceClient;
import com.telecom.order.integration.NotificationServiceClient;
import com.telecom.order.integration.ProvisioningServiceClient;
import com.telecom.order.integration.dto.CustomerProfile;
import com.telecom.order.integration.dto.CustomerValidationResult;
import com.telecom.order.integration.dto.InventoryReservationResult;
import com.telecom.order.integration.dto.InventoryReserveRequest;
import com.telecom.order.integration.dto.InventoryResourceItem;
import com.telecom.order.integration.dto.NotificationNotifyRequest;
import com.telecom.order.integration.dto.ProvisioningCreateRequest;
import com.telecom.order.integration.dto.ProvisioningRequestResult;
import com.telecom.order.mapper.OrderMapper;
import com.telecom.order.repository.OrderHistoryRepository;
import com.telecom.order.repository.OrderItemRepository;
import com.telecom.order.repository.OrderRepository;
import com.telecom.order.repository.PaymentRepository;
import com.telecom.order.repository.PromotionRepository;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Order lifecycle orchestration.
 *
 * <p>Phase 3 covers create → validate → submit → payment → cancel/retry/
 * reprocess/rollback/modify/bulk/promotions entirely inside order-service.
 * Customer/inventory/provisioning calls arrive in Phase 7 (REST), so
 * {@code submit} deliberately stops at PAYMENT_PENDING for now.
 */
@Service
public class OrderService {

  private static final Logger log = LoggerFactory.getLogger(OrderService.class);

  private final OrderRepository orders;
  private final OrderItemRepository items;
  private final OrderHistoryRepository history;
  private final PaymentRepository payments;
  private final PromotionRepository promotions;
  private final Validator validator;
  private final CustomerServiceClient customerClient;
  private final InventoryServiceClient inventoryClient;
  private final ProvisioningServiceClient provisioningClient;
  private final NotificationServiceClient notificationClient;

  public OrderService(OrderRepository orders, OrderItemRepository items,
      OrderHistoryRepository history, PaymentRepository payments,
      PromotionRepository promotions, Validator validator,
      CustomerServiceClient customerClient, InventoryServiceClient inventoryClient,
      ProvisioningServiceClient provisioningClient, NotificationServiceClient notificationClient) {
    this.orders = orders;
    this.items = items;
    this.history = history;
    this.payments = payments;
    this.promotions = promotions;
    this.validator = validator;
    this.customerClient = customerClient;
    this.inventoryClient = inventoryClient;
    this.provisioningClient = provisioningClient;
    this.notificationClient = notificationClient;
  }

  // ---------- create / read ----------

  @Transactional
  public OrderResponse create(CreateOrderRequest req) {
    CustomerOrder o = new CustomerOrder();
    o.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    o.setCustomerId(req.customerId());
    o.setCustomerNumber(req.customerNumber());
    o.setOrderType(req.orderType());
    o.setPriority(req.priority() != null ? req.priority() : OrderPriority.NORMAL);
    o.setNotes(req.notes());
    o.setStatus(OrderStatus.CREATED);
    o.setTotalAmount(BigDecimal.ZERO);
    o.setDiscountAmount(BigDecimal.ZERO);
    CustomerOrder saved = orders.save(o);
    addItems(saved, req.items());
    saved.setTotalAmount(totalOf(saved.getId()));
    record(saved, "ORDER_CREATED", null, OrderStatus.CREATED,
        req.items().size() + " item(s), type " + req.orderType());
    log("ORDER_CREATED", saved);
    return OrderMapper.toResponse(saved, items);
  }

  @Transactional
  public List<BulkOrderItemResult> bulkCreate(BulkCreateOrderRequest req) {
    List<BulkOrderItemResult> results = new ArrayList<>();
    for (CreateOrderRequest item : req.orders()) {
      var violations = validator.validate(item);
      if (!violations.isEmpty()) {
        String error = violations.stream()
            .map(v -> v.getPropertyPath() + ": " + v.getMessage())
            .sorted()
            .reduce((a, b) -> a + "; " + b)
            .orElse("invalid request");
        log.warn("service=order-service correlationId={} event=BULK_ITEM_REJECTED error={}",
            correlationId(), error);
        results.add(new BulkOrderItemResult(false, null, null, error));
        continue;
      }
      try {
        OrderResponse created = create(item);
        results.add(new BulkOrderItemResult(true, created.orderNumber(), created.id(), null));
      } catch (RuntimeException ex) {
        // Partial failure: record the error per item, keep the rest (corporate bulk scenario).
        log.warn("service=order-service correlationId={} event=BULK_ITEM_FAILED error={}",
            correlationId(), ex.getMessage());
        results.add(new BulkOrderItemResult(false, null, null, ex.getMessage()));
      }
    }
    return results;
  }

  @Transactional(readOnly = true)
  public Page<OrderResponse> list(Long customerId, OrderStatus status, Pageable pageable) {
    Page<CustomerOrder> page;
    if (customerId != null && status != null) page = orders.findByCustomerIdAndStatus(customerId, status, pageable);
    else if (customerId != null) page = orders.findByCustomerId(customerId, pageable);
    else if (status != null) page = orders.findByStatus(status, pageable);
    else page = orders.findAll(pageable);
    return page.map(o -> OrderMapper.toResponse(o, items));
  }

  @Transactional(readOnly = true)
  public Page<OrderResponse> search(String q, Pageable pageable) {
    return orders.search(q, pageable).map(o -> OrderMapper.toResponse(o, items));
  }

  @Transactional(readOnly = true)
  public OrderResponse getById(Long id) {
    return OrderMapper.toResponse(getOrder(id), items);
  }

  @Transactional(readOnly = true)
  public OrderResponse getByNumber(String orderNumber) {
    return orders.findByOrderNumber(orderNumber)
        .map(o -> OrderMapper.toResponse(o, items))
        .orElseThrow(() -> new NoSuchElementException("Order not found: " + orderNumber));
  }

  @Transactional(readOnly = true)
  public OrderStatusResponse status(Long id) {
    CustomerOrder o = getOrder(id);
    return new OrderStatusResponse(o.getId(), o.getOrderNumber(), o.getStatus());
  }

  @Transactional(readOnly = true)
  public List<OrderHistoryEntry> history(Long id) {
    getOrder(id);
    return history.findByOrderIdOrderByCreatedAtAscIdAsc(id).stream()
        .map(h -> new OrderHistoryEntry(h.getId(), h.getEvent(), h.getFromStatus(),
            h.getToStatus(), h.getComment(), h.getCreatedAt()))
        .toList();
  }

  @Transactional(readOnly = true)
  public OrderTimelineResponse timeline(Long id) {
    CustomerOrder o = getOrder(id);
    Stream<TimelineEvent> historyEvents = history.findByOrderIdOrderByCreatedAtAscIdAsc(id).stream()
        .map(h -> new TimelineEvent(h.getCreatedAt(), h.getEvent(),
            (h.getFromStatus() != null ? h.getFromStatus() + " → " : "")
                + (h.getToStatus() != null ? h.getToStatus() : "")
                + (h.getComment() != null ? " — " + h.getComment() : "")));
    Stream<TimelineEvent> paymentEvents = payments.findByOrderIdOrderByCreatedAtDesc(id).stream()
        .map(p -> new TimelineEvent(p.getCreatedAt(), "PAYMENT_" + p.getStatus(),
            p.getPaymentReference() + " " + p.getAmount() + " via " + p.getMethod()));
    List<TimelineEvent> events = Stream.concat(historyEvents, paymentEvents)
        .sorted(Comparator.comparing(TimelineEvent::timestamp)).toList();
    return new OrderTimelineResponse(o.getId(), o.getOrderNumber(), events);
  }

  // ---------- lifecycle ----------

  @Transactional
  public OrderResponse update(Long id, UpdateOrderRequest req) {
    CustomerOrder o = getOrder(id);
    requireEditable(o);
    if (req.priority() != null) o.setPriority(req.priority());
    if (req.notes() != null) o.setNotes(req.notes());
    record(o, "ORDER_UPDATED", o.getStatus(), o.getStatus(), "notes/priority updated");
    log("ORDER_UPDATED", o);
    return OrderMapper.toResponse(o, items);
  }

  @Transactional
  public OrderResponse validate(Long id) {
    CustomerOrder o = getOrder(id);
    if (o.getStatus() != OrderStatus.CREATED && o.getStatus() != OrderStatus.RETRYING) {
      throw new IllegalArgumentException("Only CREATED/RETRYING orders can be validated (current: " + o.getStatus() + ")");
    }
    if (items.findByOrderIdOrderByIdAsc(id).isEmpty()) {
      return fail(o, "Order has no items");
    }
    transition(o, OrderStatus.VALIDATING, "validation started");
    // Phase 7: synchronous REST check against customer-service (pre-Kafka).
    CustomerValidationResult customer = customerClient.validateCustomer(o.getCustomerId());
    if (customer == null || !customer.valid()) {
      String reason = "CUSTOMER_INVALID: " + (customer == null ? "no response" : customer.reasons());
      return fail(o, reason);
    }
    // NOTE: inventory eligibility REST checks plug in here in Phase 10.
    transition(o, OrderStatus.VALIDATED, "all local checks passed");
    log("ORDER_VALIDATED", o);
    return OrderMapper.toResponse(o, items);
  }

  @Transactional
  public OrderResponse submit(Long id) {
    CustomerOrder o = getOrder(id);
    if (o.getStatus() != OrderStatus.VALIDATED) {
      throw new IllegalArgumentException("Only VALIDATED orders can be submitted (current: " + o.getStatus() + ")");
    }
    // Phase 7 will call inventory reserve + provisioning here; stop at PAYMENT_PENDING in Phase 3.
    transition(o, OrderStatus.PAYMENT_PENDING, "submitted, awaiting payment");
    log("ORDER_SUBMITTED", o);
    return OrderMapper.toResponse(o, items);
  }

  @Transactional
  public OrderResponse cancel(Long id) {
    CustomerOrder o = getOrder(id);
    if (o.getStatus() == OrderStatus.COMPLETED || o.getStatus() == OrderStatus.CANCELLED) {
      throw new IllegalArgumentException("Order cannot be cancelled from " + o.getStatus());
    }
    transition(o, OrderStatus.CANCELLED, "cancelled by request");
    log("ORDER_CANCELLED", o);
    return OrderMapper.toResponse(o, items);
  }

  @Transactional
  public OrderResponse retry(Long id) {
    CustomerOrder o = getOrder(id);
    if (o.getStatus() != OrderStatus.FAILED) {
      throw new IllegalArgumentException("Only FAILED orders can be retried (current: " + o.getStatus() + ")");
    }
    transition(o, OrderStatus.RETRYING, "retry requested, awaiting validation");
    log("ORDER_RETRYING", o);
    return OrderMapper.toResponse(o, items);
  }

  @Transactional
  public OrderResponse reprocess(Long id) {
    CustomerOrder o = getOrder(id);
    if (o.getStatus() != OrderStatus.FAILED && o.getStatus() != OrderStatus.RETRYING
        && o.getStatus() != OrderStatus.CANCELLED) {
      throw new IllegalArgumentException("Only FAILED/RETRYING/CANCELLED orders can be reprocessed (current: " + o.getStatus() + ")");
    }
    o.setStatus(OrderStatus.CREATED);
    record(o, "ORDER_REPROCESSED", null, OrderStatus.CREATED, "restarted from scratch, re-validate to proceed");
    log("ORDER_REPROCESSED", o);
    return OrderMapper.toResponse(o, items);
  }

  @Transactional
  public OrderResponse rollback(Long id) {
    CustomerOrder o = getOrder(id);
    if (o.getStatus() != OrderStatus.PROVISIONING && o.getStatus() != OrderStatus.ACTIVATING
        && o.getStatus() != OrderStatus.INVENTORY_RESERVED
        && o.getStatus() != OrderStatus.PAYMENT_COMPLETED
        && o.getStatus() != OrderStatus.FAILED) {
      throw new IllegalArgumentException("Order cannot be rolled back from " + o.getStatus());
    }
    // Phase 10 will release inventory + roll back provisioning here; simulate the two-step flow.
    transition(o, OrderStatus.ROLLING_BACK, "compensation started");
    transition(o, OrderStatus.CANCELLED, "compensation completed");
    log("ORDER_ROLLED_BACK", o);
    return OrderMapper.toResponse(o, items);
  }

  @Transactional
  public OrderResponse modify(Long id, ModifyOrderRequest req) {
    CustomerOrder o = getOrder(id);
    if (o.getStatus() != OrderStatus.CREATED && o.getStatus() != OrderStatus.VALIDATED) {
      throw new IllegalArgumentException("Only CREATED/VALIDATED orders can be modified (current: " + o.getStatus() + ")");
    }
    items.deleteByOrderId(id);
    items.flush();
    addItems(o, req.items());
    o.setTotalAmount(totalOf(id));
    // Promotion must be re-applied after items change (prevents stale discounts).
    o.setPromoCode(null);
    o.setDiscountAmount(BigDecimal.ZERO);
    if (o.getStatus() == OrderStatus.VALIDATED) {
      transition(o, OrderStatus.CREATED, "items replaced, re-validation required");
    } else {
      record(o, "ORDER_MODIFIED", OrderStatus.CREATED, OrderStatus.CREATED, "items replaced");
    }
    log("ORDER_MODIFIED", o);
    return OrderMapper.toResponse(o, items);
  }

  @Transactional
  public OrderResponse applyPromotion(Long id, ApplyPromotionRequest req) {
    CustomerOrder o = getOrder(id);
    if (o.getStatus() != OrderStatus.CREATED && o.getStatus() != OrderStatus.VALIDATED) {
      throw new IllegalArgumentException("Promotions apply only to CREATED/VALIDATED orders (current: " + o.getStatus() + ")");
    }
    if (req.promoCode().equals(o.getPromoCode())) {
      throw new IllegalArgumentException("Promotion already applied: " + req.promoCode());
    }
    Promotion p = promotions.findByPromoCode(req.promoCode())
        .orElseThrow(() -> new NoSuchElementException("Promotion not found: " + req.promoCode()));
    if (!p.isActive()) throw new IllegalArgumentException("Promotion is inactive: " + req.promoCode());
    LocalDate today = LocalDate.now();
    if ((p.getValidFrom() != null && today.isBefore(p.getValidFrom()))
        || (p.getValidTo() != null && today.isAfter(p.getValidTo()))) {
      throw new IllegalArgumentException("Promotion is outside its validity window: " + req.promoCode());
    }
    BigDecimal discount = p.getDiscountType() == DiscountType.PERCENTAGE
        ? o.getTotalAmount().multiply(p.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        : p.getDiscountValue();
    o.setDiscountAmount(discount.min(o.getTotalAmount()));
    o.setPromoCode(p.getPromoCode());
    record(o, "PROMOTION_APPLIED", o.getStatus(), o.getStatus(),
        p.getPromoCode() + " discount " + o.getDiscountAmount());
    log("PROMOTION_APPLIED", o);
    return OrderMapper.toResponse(o, items);
  }

  // ---------- fulfillment saga (Phase 10) ----------

  /**
   * End-to-end fulfillment for a PAYMENT_COMPLETED order:
   * reserve → INVENTORY_RESERVED → provision (create/start/activate) →
   * ACTIVATING → COMPLETED → notify (best effort).
   *
   * <p>Any failed step compensates (release reservation, roll back provisioning)
   * and parks the order in FAILED with evidence. {@code failAt} is a demo hook
   * (RESERVE/ACTIVATE/NOTIFY) used to force failure paths in Phase 10/13 demos.
   */
  @Transactional
  public FulfillmentResponse fulfill(Long id, String failAt) {
    CustomerOrder o = getOrder(id);
    if (o.getStatus() != OrderStatus.PAYMENT_COMPLETED) {
      throw new IllegalArgumentException(
          "Only PAYMENT_COMPLETED orders can be fulfilled (current: " + o.getStatus() + ")");
    }
    List<FulfillmentStep> steps = new ArrayList<>();
    record(o, "FULFILLMENT_STARTED", o.getStatus(), o.getStatus(), "saga started");
    log("FULFILLMENT_STARTED", o);

    List<OrderItem> lines = items.findByOrderIdOrderByIdAsc(id);
    OrderItem first = lines.stream().filter(i -> resourceTypeFor(i.getItemType()) != null).findFirst().orElse(null);

    // --- step 1: reserve ---
    InventoryReservationResult reservation = null;
    String reservedIdentifier = null;
    if ("RESERVE".equalsIgnoreCase(failAt)) {
      return failFulfillment(o, steps, "RESERVE", "INVENTORY_SHORTAGE (injected): no free resource");
    }
    if (first == null) {
      steps.add(new FulfillmentStep("RESERVE", "SKIPPED", "no reservable item on order"));
    } else {
      try {
        List<InventoryResourceItem> free = inventoryClient.firstAvailable(resourceTypeFor(first.getItemType()));
        if (free.isEmpty()) {
          return failFulfillment(o, steps, "RESERVE", "INVENTORY_SHORTAGE: no AVAILABLE " + resourceTypeFor(first.getItemType()));
        }
        reservation = inventoryClient.reserve(
            new InventoryReserveRequest(free.get(0).id(), o.getId(), o.getCustomerId(), 60));
        reservedIdentifier = free.get(0).identifier();
        steps.add(new FulfillmentStep("RESERVE", "SUCCESS", reservation.reservationNumber() + " (" + reservation.resourceNumber() + ")"));
        transition(o, OrderStatus.INVENTORY_RESERVED, "reservation " + reservation.reservationNumber());
      } catch (RuntimeException ex) {
        return failFulfillment(o, steps, "RESERVE", "inventory call failed: " + ex.getMessage());
      }
    }

    // --- step 2: provision ---
    String serviceType = first == null ? "MOBILE" : serviceTypeFor(first.getItemType());
    String msisdn = first != null ? first.getMsisdn() : null;
    String resourceNumber = reservation != null ? reservation.resourceNumber() : null;
    if (msisdn == null && "MSISDN".equals(first != null ? resourceTypeFor(first.getItemType()) : null)) {
      // The reserved number itself is the MSISDN to activate.
      msisdn = reservedIdentifier;
    }
    if ("ACTIVATE".equalsIgnoreCase(failAt)) {
      msisdn = null;
      resourceNumber = null;
    }
    ProvisioningRequestResult prv;
    try {
      prv = provisioningClient.create(new ProvisioningCreateRequest(o.getId(), o.getCustomerId(),
          serviceType, msisdn, resourceNumber, first != null ? first.getProductCode() : null));
      steps.add(new FulfillmentStep("PROVISION_CREATE", "SUCCESS", prv.requestNumber()));
      transition(o, OrderStatus.PROVISIONING, "request " + prv.requestNumber());
      provisioningClient.start(prv.id());
      ProvisioningRequestResult activated = provisioningClient.activate(prv.id());
      if (!"COMPLETED".equals(activated.status())) {
        compensate(reservation, prv.id());
        return failFulfillment(o, steps, "PROVISION_ACTIVATE", String.valueOf(activated.lastError()));
      }
      steps.add(new FulfillmentStep("PROVISION_ACTIVATE", "SUCCESS", prv.requestNumber() + " active"));
      transition(o, OrderStatus.ACTIVATING, "activation confirmed");
      if (reservation != null) {
        try {
          InventoryReservationResult confirmed = inventoryClient.confirm(reservation.id());
          steps.add(new FulfillmentStep("RESERVE_CONFIRM", "SUCCESS",
              confirmed.reservationNumber() + " → " + confirmed.status()));
        } catch (RuntimeException ex) {
          steps.add(new FulfillmentStep("RESERVE_CONFIRM", "FAILED",
              "confirm failed (non-blocking): " + ex.getMessage()));
        }
      }
    } catch (RuntimeException ex) {
      compensate(reservation, null);
      return failFulfillment(o, steps, "PROVISION", "provisioning call failed: " + ex.getMessage());
    }

    // --- step 3: complete + notify (best effort) ---
    transition(o, OrderStatus.COMPLETED, "fulfillment saga completed");
    log("ORDER_COMPLETED", o);
    try {
      String recipient = "NOTIFY".equalsIgnoreCase(failAt) ? "fail-test@example.com" : customerPhone(o);
      if (recipient == null) {
        steps.add(new FulfillmentStep("NOTIFY", "SKIPPED", "no customer phone available"));
      } else {
        notificationClient.notify(new NotificationNotifyRequest(o.getId(), o.getCustomerId(), "SMS",
            recipient, "ORDER_COMPLETED",
            Map.of("orderNumber", o.getOrderNumber(), "msisdn", msisdn != null ? msisdn : "-")));
        steps.add(new FulfillmentStep("NOTIFY", "SUCCESS", "ORDER_COMPLETED to " + recipient));
      }
    } catch (RuntimeException ex) {
      steps.add(new FulfillmentStep("NOTIFY", "FAILED", "notification failed (non-blocking): " + ex.getMessage()));
    }
    record(o, "FULFILLMENT_COMPLETED", OrderStatus.ACTIVATING, OrderStatus.COMPLETED, steps.size() + " steps");
    return new FulfillmentResponse(o.getId(), o.getOrderNumber(), o.getStatus().name(), steps);
  }

  private FulfillmentResponse failFulfillment(CustomerOrder o, List<FulfillmentStep> steps,
      String step, String reason) {
    steps.add(new FulfillmentStep(step, "FAILED", reason));
    OrderStatus from = o.getStatus();
    o.setStatus(OrderStatus.FAILED);
    record(o, "FULFILLMENT_FAILED", from, OrderStatus.FAILED, reason);
    log("FULFILLMENT_FAILED", o);
    return new FulfillmentResponse(o.getId(), o.getOrderNumber(), o.getStatus().name(), steps);
  }

  /** Best-effort compensation: free the hold, roll back provisioning. */
  private void compensate(InventoryReservationResult reservation, Long provisioningId) {
    if (reservation != null) {
      try {
        inventoryClient.cancel(reservation.id());
      } catch (RuntimeException ignored) {
        // compensation must never mask the original failure
      }
    }
    if (provisioningId != null) {
      try {
        provisioningClient.rollback(provisioningId);
      } catch (RuntimeException ignored) {
        // provisioning-service has no rollback endpoint dependency here; best effort
      }
    }
  }

  private String customerPhone(CustomerOrder o) {
    try {
      CustomerProfile p = customerClient.getCustomer(o.getCustomerId());
      return p == null ? null : p.phone();
    } catch (RuntimeException e) {
      return null;
    }
  }

  private String resourceTypeFor(OrderItemType t) {
    return switch (t) {
      case SIM -> "SIM";
      case ESIM -> "ESIM";
      case MOBILE_PLAN, ROAMING_PACK -> "MSISDN";
      case DEVICE -> "DEVICE";
      case BROADBAND -> "FIBER_PORT";
      case FIBER_EQUIPMENT -> "ONT";
      case ADDON -> null;
    };
  }

  private String serviceTypeFor(OrderItemType t) {
    return switch (t) {
      case SIM, MOBILE_PLAN, ADDON -> "MOBILE";
      case ESIM -> "ESIM";
      case ROAMING_PACK -> "ROAMING";
      case DEVICE -> "DEVICE";
      case BROADBAND -> "BROADBAND";
      case FIBER_EQUIPMENT -> "FIBER";
    };
  }

  // ---------- helpers ----------

  CustomerOrder getOrder(Long id) {
    return orders.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
  }

  private void requireEditable(CustomerOrder o) {
    if (o.getStatus() == OrderStatus.COMPLETED || o.getStatus() == OrderStatus.CANCELLED) {
      throw new IllegalArgumentException("Order cannot be edited from " + o.getStatus());
    }
  }

  private void addItems(CustomerOrder o, List<OrderItemRequest> reqs) {
    for (OrderItemRequest r : reqs) {
      OrderItem i = new OrderItem();
      i.setOrder(o);
      i.setItemType(r.itemType());
      i.setProductCode(r.productCode().trim());
      i.setProductName(r.productName().trim());
      i.setQuantity(r.quantity());
      i.setUnitPrice(r.unitPrice());
      i.setMsisdn(r.msisdn());
      items.save(i);
    }
  }

  private BigDecimal totalOf(Long orderId) {
    return items.findByOrderIdOrderByIdAsc(orderId).stream()
        .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private void transition(CustomerOrder o, OrderStatus to, String comment) {
    OrderStatus from = o.getStatus();
    o.setStatus(to);
    record(o, "STATUS_CHANGED", from, to, comment);
  }

  private OrderResponse fail(CustomerOrder o, String reason) {
    OrderStatus from = o.getStatus();
    o.setStatus(OrderStatus.FAILED);
    record(o, "ORDER_FAILED", from, OrderStatus.FAILED, reason);
    log("ORDER_FAILED", o);
    return OrderMapper.toResponse(o, items);
  }

  private void record(CustomerOrder o, String event, OrderStatus from, OrderStatus to, String comment) {
    OrderHistory h = new OrderHistory();
    h.setOrder(o);
    h.setEvent(event);
    h.setFromStatus(from);
    h.setToStatus(to);
    h.setComment(comment);
    history.save(h);
  }

  private void log(String event, CustomerOrder o) {
    log.info("service=order-service correlationId={} orderId={} customerId={} event={} status={}",
        correlationId(), o.getId(), o.getCustomerId(), event, o.getStatus());
  }

  private String correlationId() {
    return MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
  }
}
