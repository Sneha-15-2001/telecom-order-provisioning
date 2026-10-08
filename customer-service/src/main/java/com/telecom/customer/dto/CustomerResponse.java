package com.telecom.customer.dto;

import com.telecom.customer.entity.CustomerStatus;
import com.telecom.customer.entity.CustomerType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Customer profile")
public record CustomerResponse(
    Long id,
    String customerNumber,
    String firstName,
    String lastName,
    String email,
    String phone,
    LocalDate dateOfBirth,
    CustomerType customerType,
    CustomerStatus status,
    Long corporateAccountId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
