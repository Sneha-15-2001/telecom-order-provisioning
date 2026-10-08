package com.telecom.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to apply a promotion code to an order")
public record ApplyPromotionRequest(
    @NotBlank @Schema(example = "FESTIVE10") String promoCode) {}
