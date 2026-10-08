package com.telecom.order.dto;

import com.telecom.order.entity.OrderItemType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "One product line on a create/modify order request")
public record OrderItemRequest(
    @NotNull @Schema(example = "MOBILE_PLAN") OrderItemType itemType,
    @NotBlank @Size(max = 50) @Schema(example = "PLAN_5G_299") String productCode,
    @NotBlank @Size(max = 150) @Schema(example = "5G Unlimited 299") String productName,
    @Min(1) @Schema(example = "1") int quantity,
    @NotNull @DecimalMin("0.00") @Schema(example = "299.00") BigDecimal unitPrice,
    @Pattern(regexp = "^[0-9]{7,15}$", message = "must be 7-15 digits")
        @Schema(example = "919876543210")
        String msisdn) {}
