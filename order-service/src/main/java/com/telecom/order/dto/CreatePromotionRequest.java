package com.telecom.order.dto;

import com.telecom.order.entity.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Request to add a promotion to the catalog")
public record CreatePromotionRequest(
    @NotBlank @Size(max = 50) @Schema(example = "FESTIVE10") String promoCode,
    @NotBlank @Size(max = 500) @Schema(example = "Festive season 10% off") String description,
    @NotNull @Schema(example = "PERCENTAGE") DiscountType discountType,
    @NotNull @DecimalMin("0.01") @Schema(example = "10") BigDecimal discountValue,
    @Schema(example = "true") boolean active,
    LocalDate validFrom,
    LocalDate validTo) {}
