package com.telecom.order.dto;

import com.telecom.order.entity.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Promotion catalog entry")
public record PromotionResponse(
    Long id,
    String promoCode,
    String description,
    DiscountType discountType,
    BigDecimal discountValue,
    boolean active,
    LocalDate validFrom,
    LocalDate validTo) {}
