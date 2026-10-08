package com.telecom.customer.dto;

import com.telecom.customer.entity.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Customer address")
public record AddressResponse(
    Long id,
    Long customerId,
    AddressType addressType,
    String street,
    String city,
    String state,
    String postalCode,
    String country,
    boolean primaryAddress,
    LocalDateTime createdAt) {}
