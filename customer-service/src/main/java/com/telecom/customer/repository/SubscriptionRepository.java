package com.telecom.customer.repository;

import com.telecom.customer.entity.Subscription;
import com.telecom.customer.entity.SubscriptionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

  List<Subscription> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

  List<Subscription> findByCustomerIdAndStatus(Long customerId, SubscriptionStatus status);

  Optional<Subscription> findBySubscriptionNumber(String subscriptionNumber);

  Optional<Subscription> findByMsisdn(String msisdn);
}
