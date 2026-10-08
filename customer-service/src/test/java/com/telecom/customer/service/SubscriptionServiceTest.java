package com.telecom.customer.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.telecom.customer.dto.SubscriptionRequest;
import com.telecom.customer.dto.SubscriptionStatusUpdateRequest;
import com.telecom.customer.entity.Customer;
import com.telecom.customer.entity.Subscription;
import com.telecom.customer.entity.SubscriptionStatus;
import com.telecom.customer.repository.CustomerRepository;
import com.telecom.customer.repository.SubscriptionRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock SubscriptionRepository subscriptions;
  @Mock CustomerRepository customers;

  @InjectMocks SubscriptionService service;

  private Customer customer(Long id) {
    Customer c = new Customer();
    ReflectionTestUtils.setField(c, "id", id);
    return c;
  }

  private Subscription subscription(Long id, Long customerId, SubscriptionStatus status) {
    Subscription s = new Subscription();
    ReflectionTestUtils.setField(s, "id", id);
    s.setCustomer(customer(customerId));
    s.setStatus(status);
    return s;
  }

  @Test
  void createAssignsNumberAndActive() {
    when(customers.findById(1L)).thenReturn(Optional.of(customer(1L)));
    when(subscriptions.save(any(Subscription.class))).thenAnswer(i -> i.getArgument(0));

    var res = service.create(1L, new SubscriptionRequest("PLAN_5G_299", "5G 299", "919000000001", null));
    assertThat(res.subscriptionNumber()).startsWith("SUB-");
    assertThat(res.status()).isEqualTo(SubscriptionStatus.ACTIVE);
  }

  @Test
  void createRejectsDuplicateMsisdn() {
    when(customers.findById(1L)).thenReturn(Optional.of(customer(1L)));
    when(subscriptions.findByMsisdn("919000000001")).thenReturn(Optional.of(subscription(2L, 1L, SubscriptionStatus.ACTIVE)));

    assertThatThrownBy(() -> service.create(1L,
        new SubscriptionRequest("PLAN_5G_299", "5G 299", "919000000001", null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("MSISDN already in use");
  }

  @Test
  void cancelStampsEndDate() {
    when(subscriptions.findById(9L)).thenReturn(Optional.of(subscription(9L, 1L, SubscriptionStatus.ACTIVE)));

    var res = service.cancel(1L, 9L);
    assertThat(res.status()).isEqualTo(SubscriptionStatus.CANCELLED);
    assertThat(res.endDate()).isNotNull();
  }

  @Test
  void doubleCancelRejected() {
    when(subscriptions.findById(8L)).thenReturn(Optional.of(subscription(8L, 1L, SubscriptionStatus.CANCELLED)));

    assertThatThrownBy(() -> service.cancel(1L, 8L)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void foreignSubscriptionRejected() {
    when(subscriptions.findById(7L)).thenReturn(Optional.of(subscription(7L, 2L, SubscriptionStatus.ACTIVE)));

    assertThatThrownBy(() -> service.cancel(1L, 7L)).isInstanceOf(NoSuchElementException.class);
  }

  @Test
  void updateStatusSuspends() {
    when(subscriptions.findById(6L)).thenReturn(Optional.of(subscription(6L, 1L, SubscriptionStatus.ACTIVE)));

    var res = service.updateStatus(1L, 6L, new SubscriptionStatusUpdateRequest(SubscriptionStatus.SUSPENDED));
    assertThat(res.status()).isEqualTo(SubscriptionStatus.SUSPENDED);
  }

  @Test
  void listReturnsMapped() {
    when(customers.findById(1L)).thenReturn(Optional.of(customer(1L)));
    when(subscriptions.findByCustomerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
    assertThat(service.list(1L)).isEmpty();
  }
}
