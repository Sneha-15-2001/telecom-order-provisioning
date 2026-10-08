package com.telecom.customer.service;

import com.telecom.customer.config.CorrelationIdFilter;
import com.telecom.customer.dto.SubscriptionRequest;
import com.telecom.customer.dto.SubscriptionResponse;
import com.telecom.customer.dto.SubscriptionStatusUpdateRequest;
import com.telecom.customer.entity.Customer;
import com.telecom.customer.entity.Subscription;
import com.telecom.customer.entity.SubscriptionStatus;
import com.telecom.customer.mapper.CustomerMapper;
import com.telecom.customer.repository.CustomerRepository;
import com.telecom.customer.repository.SubscriptionRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Plan/MSISDN subscriptions owned by a customer. */
@Service
public class SubscriptionService {

  private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

  private final SubscriptionRepository subscriptions;
  private final CustomerRepository customers;

  public SubscriptionService(SubscriptionRepository subscriptions, CustomerRepository customers) {
    this.subscriptions = subscriptions;
    this.customers = customers;
  }

  @Transactional(readOnly = true)
  public List<SubscriptionResponse> list(Long customerId) {
    getCustomer(customerId);
    return subscriptions.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
        .map(CustomerMapper::toResponse).toList();
  }

  @Transactional
  public SubscriptionResponse create(Long customerId, SubscriptionRequest req) {
    Customer c = getCustomer(customerId);
    if (req.msisdn() != null) {
      subscriptions.findByMsisdn(req.msisdn()).ifPresent(s -> {
        throw new IllegalArgumentException("MSISDN already in use: " + req.msisdn());
      });
    }
    Subscription s = new Subscription();
    s.setCustomer(c);
    s.setSubscriptionNumber("SUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    s.setPlanCode(req.planCode().trim());
    s.setPlanName(req.planName().trim());
    s.setMsisdn(req.msisdn());
    s.setStatus(SubscriptionStatus.ACTIVE);
    s.setStartDate(req.startDate() != null ? req.startDate() : LocalDate.now());
    Subscription saved = subscriptions.save(s);
    log.info("service=customer-service correlationId={} event=SUBSCRIPTION_CREATED customerId={} subscriptionId={} status=SUCCESS",
        correlationId(), customerId, saved.getId());
    return CustomerMapper.toResponse(saved);
  }

  @Transactional
  public SubscriptionResponse updateStatus(Long customerId, Long subscriptionId, SubscriptionStatusUpdateRequest req) {
    Subscription s = getSubscription(customerId, subscriptionId);
    s.setStatus(req.status());
    if (req.status() == SubscriptionStatus.CANCELLED) {
      s.setEndDate(LocalDate.now());
    }
    log.info("service=customer-service correlationId={} event=SUBSCRIPTION_STATUS_CHANGED customerId={} subscriptionId={} status={}",
        correlationId(), customerId, subscriptionId, req.status());
    return CustomerMapper.toResponse(s);
  }

  @Transactional
  public SubscriptionResponse cancel(Long customerId, Long subscriptionId) {
    Subscription s = getSubscription(customerId, subscriptionId);
    if (s.getStatus() == SubscriptionStatus.CANCELLED) {
      throw new IllegalArgumentException("Subscription already cancelled: " + subscriptionId);
    }
    s.setStatus(SubscriptionStatus.CANCELLED);
    s.setEndDate(LocalDate.now());
    log.info("service=customer-service correlationId={} event=SUBSCRIPTION_CANCELLED customerId={} subscriptionId={} status=SUCCESS",
        correlationId(), customerId, subscriptionId);
    return CustomerMapper.toResponse(s);
  }

  private Subscription getSubscription(Long customerId, Long subscriptionId) {
    Subscription s = subscriptions.findById(subscriptionId)
        .orElseThrow(() -> new NoSuchElementException("Subscription not found: " + subscriptionId));
    if (!s.getCustomer().getId().equals(customerId)) {
      throw new NoSuchElementException("Subscription " + subscriptionId + " does not belong to customer " + customerId);
    }
    return s;
  }

  private Customer getCustomer(Long id) {
    return customers.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Customer not found: " + id));
  }

  private String correlationId() {
    return MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
  }
}
