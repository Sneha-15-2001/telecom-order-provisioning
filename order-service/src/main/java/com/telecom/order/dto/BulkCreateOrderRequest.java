package com.telecom.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Request to create many orders at once (partial failures reported per item)")
public record BulkCreateOrderRequest(
    // NOTE: no @Valid here by design — each item is validated programmatically
    // inside bulkCreate so one bad item never aborts the whole batch.
    @NotEmpty List<CreateOrderRequest> orders) {}
