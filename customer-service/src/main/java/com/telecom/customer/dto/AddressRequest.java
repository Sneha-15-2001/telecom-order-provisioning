package com.telecom.customer.dto;

import com.telecom.customer.entity.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to add an address to a customer")
public record AddressRequest(
    @NotNull @Schema(example = "HOME") AddressType addressType,
    @NotBlank @Size(max = 255) @Schema(example = "221 MG Road") String street,
    @NotBlank @Size(max = 100) @Schema(example = "Bengaluru") String city,
    @Size(max = 100) @Schema(example = "Karnataka") String state,
    @NotBlank @Size(max = 20) @Schema(example = "560001") String postalCode,
    @NotBlank @Size(max = 100) @Schema(example = "India") String country,
    @Schema(description = "Make this the primary address (demotes others)")
        boolean primaryAddress) {}
