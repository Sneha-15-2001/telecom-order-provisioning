package com.telecom.order.dto;

import com.telecom.order.entity.OrderItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "One persisted order line")
public record OrderItemResponse(
    Long id,
    OrderItemType itemType,
    String productCode,
    String productName,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal,
    String msisdn) {}
