package com.telecom.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a corporate (B2B) account")
public record CorporateAccountRequest(
    @NotBlank @Size(max = 200) @Schema(example = "Acme Telecom Pvt Ltd") String companyName,
    @NotBlank @Size(max = 50) @Schema(example = "CIN-U64200KA2015PTC000001")
        String registrationNumber,
    @NotBlank @Email @Size(max = 255) @Schema(example = "telecom@acme.example")
        String contactEmail,
    @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "must be 7-15 digits, optional leading +")
        @Schema(example = "+918012345678")
        String contactPhone) {}
