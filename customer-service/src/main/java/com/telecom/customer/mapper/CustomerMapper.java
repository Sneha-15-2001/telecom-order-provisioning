package com.telecom.customer.mapper;

import com.telecom.customer.dto.AddressResponse;
import com.telecom.customer.dto.CorporateAccountResponse;
import com.telecom.customer.dto.CustomerResponse;
import com.telecom.customer.dto.SubscriptionResponse;
import com.telecom.customer.entity.CorporateAccount;
import com.telecom.customer.entity.Customer;
import com.telecom.customer.entity.CustomerAddress;
import com.telecom.customer.entity.Subscription;

/** Manual entity → DTO mapping (kept explicit instead of reflection-based mappers). */
public final class CustomerMapper {

  private CustomerMapper() {}

  public static CustomerResponse toResponse(Customer c) {
    return new CustomerResponse(
        c.getId(),
        c.getCustomerNumber(),
        c.getFirstName(),
        c.getLastName(),
        c.getEmail(),
        c.getPhone(),
        c.getDateOfBirth(),
        c.getCustomerType(),
        c.getStatus(),
        c.getCorporateAccount() != null ? c.getCorporateAccount().getId() : null,
        c.getCreatedAt(),
        c.getUpdatedAt());
  }

  public static AddressResponse toResponse(CustomerAddress a) {
    return new AddressResponse(
        a.getId(),
        a.getCustomer().getId(),
        a.getAddressType(),
        a.getStreet(),
        a.getCity(),
        a.getState(),
        a.getPostalCode(),
        a.getCountry(),
        a.isPrimaryAddress(),
        a.getCreatedAt());
  }

  public static SubscriptionResponse toResponse(Subscription s) {
    return new SubscriptionResponse(
        s.getId(),
        s.getCustomer().getId(),
        s.getSubscriptionNumber(),
        s.getPlanCode(),
        s.getPlanName(),
        s.getMsisdn(),
        s.getStatus(),
        s.getStartDate(),
        s.getEndDate(),
        s.getCreatedAt());
  }

  public static CorporateAccountResponse toResponse(CorporateAccount a) {
    return new CorporateAccountResponse(
        a.getId(),
        a.getAccountNumber(),
        a.getCompanyName(),
        a.getRegistrationNumber(),
        a.getContactEmail(),
        a.getContactPhone(),
        a.getStatus(),
        a.getCreatedAt());
  }
}
