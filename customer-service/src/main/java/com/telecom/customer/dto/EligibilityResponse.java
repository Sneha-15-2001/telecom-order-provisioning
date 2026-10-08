package com.telecom.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Whether the customer may buy / provision new services")
public record EligibilityResponse(
    Long customerId, boolean eligible, List<String> reasons) {}
