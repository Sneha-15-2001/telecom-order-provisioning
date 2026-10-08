package com.telecom.customer.dto;

import com.telecom.customer.entity.CustomerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Request to onboard a new customer")
public record CreateCustomerRequest(
    @NotBlank @Size(max = 100) @Schema(example = "Aarav") String firstName,
    @NotBlank @Size(max = 100) @Schema(example = "Sharma") String lastName,
    @NotBlank @Email @Size(max = 255) @Schema(example = "aarav.sharma@example.com") String email,
    @NotBlank
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "must be 7-15 digits, optional leading +")
        @Schema(example = "+919876543210")
        String phone,
    @Past @Schema(example = "1995-04-17") LocalDate dateOfBirth,
    @NotNull @Schema(example = "INDIVIDUAL") CustomerType customerType,
    @Schema(description = "Corporate account ID for CORPORATE customers", example = "1")
        Long corporateAccountId) {}
