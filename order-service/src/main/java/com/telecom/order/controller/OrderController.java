package com.telecom.order.controller;

import com.telecom.order.dto.ApplyPromotionRequest;
import com.telecom.order.dto.BulkCreateOrderRequest;
import com.telecom.order.dto.BulkOrderItemResult;
import com.telecom.order.dto.CreateOrderRequest;
import com.telecom.order.dto.FulfillmentResponse;
import com.telecom.order.dto.ModifyOrderRequest;
import com.telecom.order.dto.OrderHistoryEntry;
import com.telecom.order.dto.OrderResponse;
import com.telecom.order.dto.OrderStatusResponse;
import com.telecom.order.dto.OrderTimelineResponse;
import com.telecom.order.dto.PaymentResponse;
import com.telecom.order.dto.PaymentValidationResponse;
import com.telecom.order.dto.RecordPaymentRequest;
import com.telecom.order.dto.UpdateOrderRequest;
import com.telecom.order.entity.OrderStatus;
import com.telecom.order.service.OrderService;
import com.telecom.order.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Order orchestration: lifecycle, payments, promotions, bulk (Phase 3). */
@Tag(name = "Orders", description = "Order orchestration (Phase 3)")
@RestController
@RequestMapping(path = "/api/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

  private final OrderService orderService;
  private final PaymentService paymentService;

  public OrderController(OrderService orderService, PaymentService paymentService) {
    this.orderService = orderService;
    this.paymentService = paymentService;
  }

  @Operation(summary = "Create an order with items")
  @ApiResponse(responseCode = "201", description = "Order created in CREATED status")
  @ApiResponse(responseCode = "400", description = "Validation failed")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
    return orderService.create(request);
  }

  @Operation(summary = "Create many orders; per-item failures are reported, not fatal")
  @ApiResponse(responseCode = "200", description = "Per-item results (check success flags)")
  @PostMapping(path = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
  public List<BulkOrderItemResult> bulk(@Valid @RequestBody BulkCreateOrderRequest request) {
    return orderService.bulkCreate(request);
  }

  @Operation(summary = "List orders (paged, optional customer and status filters)")
  @GetMapping
  public Page<OrderResponse> list(
      @RequestParam(required = false) Long customerId,
      @RequestParam(required = false) OrderStatus status,
      @ParameterObject Pageable pageable) {
    return orderService.list(customerId, status, pageable);
  }

  @Operation(summary = "Search orders by order number or customer number")
  @GetMapping("/search")
  public Page<OrderResponse> search(@RequestParam String q, @ParameterObject Pageable pageable) {
    return orderService.search(q, pageable);
  }

  @Operation(summary = "List failed orders")
  @GetMapping("/failed")
  public Page<OrderResponse> failed(@ParameterObject Pageable pageable) {
    return orderService.list(null, OrderStatus.FAILED, pageable);
  }

  @Operation(summary = "List orders awaiting payment")
  @GetMapping("/pending")
  public Page<OrderResponse> pending(@ParameterObject Pageable pageable) {
    return orderService.list(null, OrderStatus.PAYMENT_PENDING, pageable);
  }

  @Operation(summary = "Get an order by ID")
  @GetMapping("/{id}")
  public OrderResponse getById(@PathVariable Long id) {
    return orderService.getById(id);
  }

  @Operation(summary = "Get an order by order number")
  @GetMapping("/number/{orderNumber}")
  public OrderResponse getByNumber(@PathVariable String orderNumber) {
    return orderService.getByNumber(orderNumber);
  }

  @Operation(summary = "Update order notes/priority")
  @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public OrderResponse update(@PathVariable Long id, @Valid @RequestBody UpdateOrderRequest request) {
    return orderService.update(id, request);
  }

  @Operation(summary = "Current lifecycle status of an order")
  @GetMapping("/{id}/status")
  public OrderStatusResponse status(@PathVariable Long id) {
    return orderService.status(id);
  }

  @Operation(summary = "Immutable status-change history of an order")
  @GetMapping("/{id}/history")
  public List<OrderHistoryEntry> history(@PathVariable Long id) {
    return orderService.history(id);
  }

  @Operation(summary = "User-friendly timeline (history + payment events)")
  @GetMapping("/{id}/timeline")
  public OrderTimelineResponse timeline(@PathVariable Long id) {
    return orderService.timeline(id);
  }

  @Operation(summary = "Validate an order (CREATED/RETRYING → VALIDATED)")
  @PostMapping("/{id}/validate")
  public OrderResponse validate(@PathVariable Long id) {
    return orderService.validate(id);
  }

  @Operation(summary = "Submit a validated order (→ PAYMENT_PENDING)")
  @PostMapping("/{id}/submit")
  public OrderResponse submit(@PathVariable Long id) {
    return orderService.submit(id);
  }

  @Operation(summary = "Cancel an order")
  @PostMapping("/{id}/cancel")
  public OrderResponse cancel(@PathVariable Long id) {
    return orderService.cancel(id);
  }

  @Operation(summary = "Retry a failed order (→ RETRYING)")
  @PostMapping("/{id}/retry")
  public OrderResponse retry(@PathVariable Long id) {
    return orderService.retry(id);
  }

  @Operation(summary = "Reprocess an order from scratch (→ CREATED)")
  @PostMapping("/{id}/reprocess")
  public OrderResponse reprocess(@PathVariable Long id) {
    return orderService.reprocess(id);
  }

  @Operation(summary = "Roll back an in-flight order (→ ROLLING_BACK → CANCELLED)")
  @PostMapping("/{id}/rollback")
  public OrderResponse rollback(@PathVariable Long id) {
    return orderService.rollback(id);
  }

  @Operation(summary = "Replace all items on a CREATED/VALIDATED order (clears promotion)")
  @PostMapping(path = "/{id}/modify", consumes = MediaType.APPLICATION_JSON_VALUE)
  public OrderResponse modify(@PathVariable Long id, @Valid @RequestBody ModifyOrderRequest request) {
    return orderService.modify(id, request);
  }

  @Operation(summary = "Run end-to-end fulfillment (reserve → provision → complete → notify)")
  @ApiResponse(responseCode = "200", description = "Trace returned; orderStatus carries the verdict")
  @ApiResponse(responseCode = "400", description = "Order not in PAYMENT_COMPLETED")
  @ApiResponse(responseCode = "404", description = "Order not found")
  @PostMapping("/{id}/fulfill")
  public FulfillmentResponse fulfill(
      @PathVariable Long id,
      @RequestParam(required = false) String failAt) {
    return orderService.fulfill(id, failAt);
  }

  @Operation(summary = "Record a payment attempt against a PAYMENT_PENDING order")
  @PostMapping(path = "/{id}/payments", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public PaymentResponse recordPayment(@PathVariable Long id, @Valid @RequestBody RecordPaymentRequest request) {
    return paymentService.record(id, request);
  }

  @Operation(summary = "List payment attempts on an order")
  @GetMapping("/{id}/payments")
  public List<PaymentResponse> payments(@PathVariable Long id) {
    return paymentService.list(id);
  }

  @Operation(summary = "Validate the latest payment against the payable total")
  @ApiResponse(responseCode = "200", description = "Validation result (valid flag carries the verdict)")
  @ApiResponse(responseCode = "404", description = "Order not found or no payment recorded")
  @PostMapping("/{id}/payment/validate")
  public PaymentValidationResponse validatePayment(@PathVariable Long id) {
    return paymentService.validate(id);
  }

  @Operation(summary = "Apply a promotion code to an order")
  @PostMapping(path = "/{id}/promotion/apply", consumes = MediaType.APPLICATION_JSON_VALUE)
  public OrderResponse applyPromotion(@PathVariable Long id, @Valid @RequestBody ApplyPromotionRequest request) {
    return orderService.applyPromotion(id, request);
  }
}
