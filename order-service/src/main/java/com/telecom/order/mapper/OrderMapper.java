package com.telecom.order.mapper;

import com.telecom.order.dto.OrderItemResponse;
import com.telecom.order.dto.OrderResponse;
import com.telecom.order.dto.PaymentResponse;
import com.telecom.order.dto.PromotionResponse;
import com.telecom.order.entity.CustomerOrder;
import com.telecom.order.entity.OrderItem;
import com.telecom.order.entity.Payment;
import com.telecom.order.entity.Promotion;
import com.telecom.order.repository.OrderItemRepository;
import java.math.BigDecimal;
import java.util.List;

/** Explicit entity → DTO mapping. */
public final class OrderMapper {

  private OrderMapper() {}

  public static OrderResponse toResponse(CustomerOrder o, OrderItemRepository items) {
    List<OrderItemResponse> lines = items.findByOrderIdOrderByIdAsc(o.getId()).stream()
        .map(OrderMapper::toResponse).toList();
    BigDecimal payable = o.getTotalAmount().subtract(o.getDiscountAmount());
    return new OrderResponse(
        o.getId(), o.getOrderNumber(), o.getCustomerId(), o.getCustomerNumber(),
        o.getOrderType(), o.getStatus(), o.getPriority(), o.getTotalAmount(),
        o.getDiscountAmount(), payable.max(BigDecimal.ZERO), o.getPromoCode(),
        o.getNotes(), lines, o.getCreatedAt(), o.getUpdatedAt());
  }

  public static OrderItemResponse toResponse(OrderItem i) {
    BigDecimal lineTotal = i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
    return new OrderItemResponse(i.getId(), i.getItemType(), i.getProductCode(),
        i.getProductName(), i.getQuantity(), i.getUnitPrice(), lineTotal, i.getMsisdn());
  }

  public static PaymentResponse toResponse(Payment p) {
    return new PaymentResponse(p.getId(), p.getOrder().getId(), p.getPaymentReference(),
        p.getAmount(), p.getMethod(), p.getStatus(), p.getCreatedAt());
  }

  public static PromotionResponse toResponse(Promotion p) {
    return new PromotionResponse(p.getId(), p.getPromoCode(), p.getDescription(),
        p.getDiscountType(), p.getDiscountValue(), p.isActive(), p.getValidFrom(), p.getValidTo());
  }
}
