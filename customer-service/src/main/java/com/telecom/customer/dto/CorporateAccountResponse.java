package com.telecom.customer.dto;

import com.telecom.customer.entity.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Corporate (B2B) account")
public record CorporateAccountResponse(
    Long id,
    String accountNumber,
    String companyName,
    String registrationNumber,
    String contactEmail,
    String contactPhone,
    CustomerStatus status,
    LocalDateTime createdAt) {}
