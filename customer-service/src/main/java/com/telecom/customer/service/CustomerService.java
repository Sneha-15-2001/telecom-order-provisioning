package com.telecom.customer.service;

import com.telecom.customer.config.CorrelationIdFilter;
import com.telecom.customer.dto.AddressRequest;
import com.telecom.customer.dto.AddressResponse;
import com.telecom.customer.dto.CreateCustomerRequest;
import com.telecom.customer.dto.CustomerHistoryResponse;
import com.telecom.customer.dto.CustomerHistoryResponse.HistoryEvent;
import com.telecom.customer.dto.CustomerResponse;
import com.telecom.customer.dto.CustomerValidationResponse;
import com.telecom.customer.dto.EligibilityResponse;
import com.telecom.customer.dto.UpdateCustomerRequest;
import com.telecom.customer.entity.CorporateAccount;
import com.telecom.customer.entity.Customer;
import com.telecom.customer.entity.CustomerAddress;
import com.telecom.customer.entity.CustomerStatus;
import com.telecom.customer.entity.CustomerType;
import com.telecom.customer.mapper.CustomerMapper;
import com.telecom.customer.repository.CorporateAccountRepository;
import com.telecom.customer.repository.CustomerAddressRepository;
import com.telecom.customer.repository.CustomerRepository;
import com.telecom.customer.repository.SubscriptionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Customer onboarding, profile, status, eligibility, history and addresses.
 *
 * <p>{@code validate} is the contract order-service will call in Phase 7
 * before accepting an order.
 */
@Service
public class CustomerService {

  private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

  private final CustomerRepository customers;
  private final CustomerAddressRepository addresses;
  private final SubscriptionRepository subscriptions;
  private final CorporateAccountRepository corporateAccounts;

  public CustomerService(
      CustomerRepository customers,
      CustomerAddressRepository addresses,
      SubscriptionRepository subscriptions,
      CorporateAccountRepository corporateAccounts) {
    this.customers = customers;
    this.addresses = addresses;
    this.subscriptions = subscriptions;
    this.corporateAccounts = corporateAccounts;
  }

  @Transactional
  public CustomerResponse create(CreateCustomerRequest req) {
    customers.findByEmail(req.email()).ifPresent(c -> {
      throw new IllegalArgumentException("Email already registered: " + req.email());
    });
    customers.findByPhone(req.phone()).ifPresent(c -> {
      throw new IllegalArgumentException("Phone already registered: " + req.phone());
    });
    Customer c = new Customer();
    c.setCustomerNumber("CUS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    c.setFirstName(req.firstName().trim());
    c.setLastName(req.lastName().trim());
    c.setEmail(req.email().trim().toLowerCase());
    c.setPhone(req.phone().trim());
    c.setDateOfBirth(req.dateOfBirth());
    c.setCustomerType(req.customerType());
    c.setStatus(CustomerStatus.ACTIVE);
    if (req.corporateAccountId() != null) {
      CorporateAccount account = getCorporateAccount(req.corporateAccountId());
      c.setCorporateAccount(account);
      c.setCustomerType(CustomerType.CORPORATE);
    }
    Customer saved = customers.save(c);
    log.info("service=customer-service correlationId={} event=CUSTOMER_CREATED customerId={} status=SUCCESS",
        correlationId(), saved.getId());
    return CustomerMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public Page<CustomerResponse> list(CustomerStatus status, Pageable pageable) {
    Page<Customer> page = status == null ? customers.findAll(pageable) : customers.findByStatus(status, pageable);
    return page.map(CustomerMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public Page<CustomerResponse> search(String query, Pageable pageable) {
    return customers.search(query, pageable).map(CustomerMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public CustomerResponse getById(Long id) {
    return CustomerMapper.toResponse(getCustomer(id));
  }

  @Transactional(readOnly = true)
  public CustomerResponse getByNumber(String customerNumber) {
    return customers.findByCustomerNumber(customerNumber)
        .map(CustomerMapper::toResponse)
        .orElseThrow(() -> new NoSuchElementException("Customer not found: " + customerNumber));
  }

  @Transactional
  public CustomerResponse update(Long id, UpdateCustomerRequest req) {
    Customer c = getCustomer(id);
    if (req.firstName() != null) c.setFirstName(req.firstName().trim());
    if (req.lastName() != null) c.setLastName(req.lastName().trim());
    if (req.email() != null && !req.email().equalsIgnoreCase(c.getEmail())) {
      customers.findByEmail(req.email().trim().toLowerCase()).ifPresent(other -> {
        throw new IllegalArgumentException("Email already registered: " + req.email());
      });
      c.setEmail(req.email().trim().toLowerCase());
    }
    if (req.phone() != null && !req.phone().equals(c.getPhone())) {
      customers.findByPhone(req.phone().trim()).ifPresent(other -> {
        throw new IllegalArgumentException("Phone already registered: " + req.phone());
      });
      c.setPhone(req.phone().trim());
    }
    if (req.dateOfBirth() != null) c.setDateOfBirth(req.dateOfBirth());
    log.info("service=customer-service correlationId={} event=CUSTOMER_UPDATED customerId={} status=SUCCESS",
        correlationId(), id);
    return CustomerMapper.toResponse(c);
  }

  @Transactional
  public void delete(Long id) {
    Customer c = getCustomer(id);
    addresses.findByCustomerIdOrderByPrimaryAddressDescIdAsc(id).forEach(addresses::delete);
    subscriptions.findByCustomerIdOrderByCreatedAtDesc(id).forEach(subscriptions::delete);
    customers.delete(c);
    log.info("service=customer-service correlationId={} event=CUSTOMER_DELETED customerId={} status=SUCCESS",
        correlationId(), id);
  }

  @Transactional
  public CustomerResponse updateStatus(Long id, CustomerStatus status) {
    Customer c = getCustomer(id);
    c.setStatus(status);
    log.info("service=customer-service correlationId={} event=CUSTOMER_STATUS_CHANGED customerId={} status={}",
        correlationId(), id, status);
    return CustomerMapper.toResponse(c);
  }

  @Transactional
  public CustomerResponse suspend(Long id) {
    Customer c = getCustomer(id);
    if (c.getStatus() != CustomerStatus.ACTIVE) {
      throw new IllegalArgumentException("Only ACTIVE customers can be suspended (current: " + c.getStatus() + ")");
    }
    c.setStatus(CustomerStatus.SUSPENDED);
    log.info("service=customer-service correlationId={} event=CUSTOMER_SUSPENDED customerId={} status=SUCCESS",
        correlationId(), id);
    return CustomerMapper.toResponse(c);
  }

  @Transactional
  public CustomerResponse reactivate(Long id) {
    Customer c = getCustomer(id);
    if (c.getStatus() != CustomerStatus.SUSPENDED && c.getStatus() != CustomerStatus.INACTIVE) {
      throw new IllegalArgumentException("Only SUSPENDED/INACTIVE customers can be reactivated (current: " + c.getStatus() + ")");
    }
    c.setStatus(CustomerStatus.ACTIVE);
    log.info("service=customer-service correlationId={} event=CUSTOMER_REACTIVATED customerId={} status=SUCCESS",
        correlationId(), id);
    return CustomerMapper.toResponse(c);
  }

  /** Order-service pre-check contract (Phase 7): is this customer orderable? */
  @Transactional(readOnly = true)
  public CustomerValidationResponse validate(Long id) {
    Customer c = getCustomer(id);
    List<String> reasons = new ArrayList<>();
    if (c.getStatus() != CustomerStatus.ACTIVE) reasons.add("CUSTOMER_NOT_ACTIVE:" + c.getStatus());
    boolean valid = reasons.isEmpty();
    log.info("service=customer-service correlationId={} event=CUSTOMER_VALIDATED customerId={} valid={}",
        correlationId(), id, valid);
    return new CustomerValidationResponse(c.getId(), c.getCustomerNumber(), c.getStatus(), valid, reasons);
  }

  @Transactional(readOnly = true)
  public EligibilityResponse eligibility(Long id) {
    Customer c = getCustomer(id);
    List<String> reasons = new ArrayList<>();
    if (c.getStatus() == CustomerStatus.BLOCKED) reasons.add("CUSTOMER_BLOCKED");
    else if (c.getStatus() == CustomerStatus.SUSPENDED) reasons.add("CUSTOMER_SUSPENDED");
    else if (c.getStatus() != CustomerStatus.ACTIVE) reasons.add("CUSTOMER_NOT_ACTIVE:" + c.getStatus());
    if (c.getCustomerType() == CustomerType.CORPORATE && c.getCorporateAccount() != null
        && c.getCorporateAccount().getStatus() != CustomerStatus.ACTIVE) {
      reasons.add("CORPORATE_ACCOUNT_NOT_ACTIVE:" + c.getCorporateAccount().getStatus());
    }
    return new EligibilityResponse(c.getId(), reasons.isEmpty(), reasons);
  }

  @Transactional(readOnly = true)
  public CustomerHistoryResponse history(Long id) {
    Customer c = getCustomer(id);
    List<HistoryEvent> events = new ArrayList<>();
    events.add(new HistoryEvent("CUSTOMER_CREATED", "Customer " + c.getCustomerNumber() + " onboarded", c.getCreatedAt()));
    if (!c.getCreatedAt().equals(c.getUpdatedAt())) {
      events.add(new HistoryEvent("CUSTOMER_UPDATED", "Profile/status changed, current status " + c.getStatus(), c.getUpdatedAt()));
    }
    subscriptions.findByCustomerIdOrderByCreatedAtDesc(id).forEach(s ->
        events.add(new HistoryEvent("SUBSCRIPTION_CREATED",
            "Subscription " + s.getSubscriptionNumber() + " (" + s.getPlanCode() + ") status " + s.getStatus(),
            s.getCreatedAt())));
    return new CustomerHistoryResponse(c.getId(), events);
  }

  @Transactional(readOnly = true)
  public List<AddressResponse> listAddresses(Long customerId) {
    getCustomer(customerId);
    return addresses.findByCustomerIdOrderByPrimaryAddressDescIdAsc(customerId).stream()
        .map(CustomerMapper::toResponse).toList();
  }

  @Transactional
  public AddressResponse addAddress(Long customerId, AddressRequest req) {
    Customer c = getCustomer(customerId);
    if (req.primaryAddress()) {
      addresses.findByCustomerIdOrderByPrimaryAddressDescIdAsc(customerId).forEach(a -> {
        if (a.isPrimaryAddress()) a.setPrimaryAddress(false);
      });
    }
    CustomerAddress a = new CustomerAddress();
    a.setCustomer(c);
    a.setAddressType(req.addressType());
    a.setStreet(req.street().trim());
    a.setCity(req.city().trim());
    a.setState(req.state());
    a.setPostalCode(req.postalCode().trim());
    a.setCountry(req.country().trim());
    a.setPrimaryAddress(req.primaryAddress());
    CustomerAddress saved = addresses.save(a);
    log.info("service=customer-service correlationId={} event=ADDRESS_ADDED customerId={} status=SUCCESS",
        correlationId(), customerId);
    return CustomerMapper.toResponse(saved);
  }

  @Transactional
  public CustomerResponse linkCorporateAccount(Long customerId, Long accountId) {
    Customer c = getCustomer(customerId);
    CorporateAccount account = getCorporateAccount(accountId);
    c.setCorporateAccount(account);
    c.setCustomerType(CustomerType.CORPORATE);
    log.info("service=customer-service correlationId={} event=CUSTOMER_LINKED_TO_CORPORATE customerId={} accountId={} status=SUCCESS",
        correlationId(), customerId, accountId);
    return CustomerMapper.toResponse(c);
  }

  private Customer getCustomer(Long id) {
    return customers.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Customer not found: " + id));
  }

  private CorporateAccount getCorporateAccount(Long id) {
    return corporateAccounts.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Corporate account not found: " + id));
  }

  private String correlationId() {
    return MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
  }
}
