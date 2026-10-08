package com.telecom.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.telecom.customer.dto.CreateCustomerRequest;
import com.telecom.customer.dto.CustomerResponse;
import com.telecom.customer.dto.CustomerValidationResponse;
import com.telecom.customer.entity.Customer;
import com.telecom.customer.entity.CustomerStatus;
import com.telecom.customer.entity.CustomerType;
import com.telecom.customer.repository.CorporateAccountRepository;
import com.telecom.customer.repository.CustomerAddressRepository;
import com.telecom.customer.repository.CustomerRepository;
import com.telecom.customer.repository.SubscriptionRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

  @Mock CustomerRepository customers;
  @Mock CustomerAddressRepository addresses;
  @Mock SubscriptionRepository subscriptions;
  @Mock CorporateAccountRepository corporateAccounts;

  @InjectMocks CustomerService service;

  @Test
  void createAssignsNumberAndActiveStatus() {
    when(customers.findByEmail("a@x.com")).thenReturn(Optional.empty());
    when(customers.findByPhone("+911111111111")).thenReturn(Optional.empty());
    when(customers.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));

    CustomerResponse res = service.create(
        new CreateCustomerRequest("A", "B", "a@x.com", "+911111111111", null, CustomerType.INDIVIDUAL, null));

    assertThat(res.customerNumber()).startsWith("CUS-");
    assertThat(res.status()).isEqualTo(CustomerStatus.ACTIVE);
    verify(customers).save(any(Customer.class));
  }

  @Test
  void createRejectsDuplicateEmail() {
    Customer existing = new Customer();
    when(customers.findByEmail("dup@x.com")).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> service.create(
        new CreateCustomerRequest("A", "B", "dup@x.com", "+911111111111", null, CustomerType.INDIVIDUAL, null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Email already registered");
  }

  @Test
  void suspendAndReactivateFollowTransitions() {
    Customer c = new Customer();
    c.setStatus(CustomerStatus.ACTIVE);
    when(customers.findById(7L)).thenReturn(Optional.of(c));

    assertThat(service.suspend(7L).status()).isEqualTo(CustomerStatus.SUSPENDED);
    assertThat(service.reactivate(7L).status()).isEqualTo(CustomerStatus.ACTIVE);
  }

  @Test
  void suspendRejectsNonActive() {
    Customer c = new Customer();
    c.setStatus(CustomerStatus.BLOCKED);
    when(customers.findById(9L)).thenReturn(Optional.of(c));

    assertThatThrownBy(() -> service.suspend(9L)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void validateMarksBlockedCustomerInvalid() {
    Customer c = new Customer();
    c.setStatus(CustomerStatus.BLOCKED);
    when(customers.findById(3L)).thenReturn(Optional.of(c));

    CustomerValidationResponse res = service.validate(3L);
    assertThat(res.valid()).isFalse();
    assertThat(res.reasons()).anyMatch(r -> r.contains("BLOCKED"));
  }
}
