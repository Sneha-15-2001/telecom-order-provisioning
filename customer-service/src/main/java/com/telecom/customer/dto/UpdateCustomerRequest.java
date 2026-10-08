package com.telecom.customer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Request to update a customer profile (all fields optional)")
public record UpdateCustomerRequest(
    @Size(max = 100) @Schema(example = "Aarav") String firstName,
    @Size(max = 100) @Schema(example = "Sharma") String lastName,
    @Email @Size(max = 255) @Schema(example = "aarav.sharma@example.com") String email,
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "must be 7-15 digits, optional leading +")
        @Schema(example = "+919876543211")
        String phone,
    @Past @Schema(example = "1995-04-17") LocalDate dateOfBirth) {}
