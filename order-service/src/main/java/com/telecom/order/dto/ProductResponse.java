package com.telecom.order.dto;

import com.telecom.order.entity.OrderItemType;
import java.math.BigDecimal;

/** Catalogue entry as the order-entry UI consumes it. */
public record ProductResponse(
    Long id,
    String productCode,
    String name,
    OrderItemType itemType,
    BigDecimal price,
    String description,
    Integer dataGb,
    boolean active) {}