package com.telecom.order.dto;

import com.telecom.order.entity.OrderPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Editable order fields (notes/priority only; items change via modify)")
public record UpdateOrderRequest(
    @Schema(example = "HIGH") OrderPriority priority,
    @Size(max = 1000) String notes) {}
