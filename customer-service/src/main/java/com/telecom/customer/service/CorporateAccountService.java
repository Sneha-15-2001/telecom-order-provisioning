package com.telecom.customer.service;

import com.telecom.customer.config.CorrelationIdFilter;
import com.telecom.customer.dto.CorporateAccountRequest;
import com.telecom.customer.dto.CorporateAccountResponse;
import com.telecom.customer.entity.CorporateAccount;
import com.telecom.customer.entity.CustomerStatus;
import com.telecom.customer.mapper.CustomerMapper;
import com.telecom.customer.repository.CorporateAccountRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Corporate (B2B) accounts. */
@Service
public class CorporateAccountService {

  private static final Logger log = LoggerFactory.getLogger(CorporateAccountService.class);

  private final CorporateAccountRepository accounts;

  public CorporateAccountService(CorporateAccountRepository accounts) {
    this.accounts = accounts;
  }

  @Transactional
  public CorporateAccountResponse create(CorporateAccountRequest req) {
    accounts.findByRegistrationNumber(req.registrationNumber()).ifPresent(a -> {
      throw new IllegalArgumentException("Registration number already exists: " + req.registrationNumber());
    });
    CorporateAccount a = new CorporateAccount();
    a.setAccountNumber("CORP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    a.setCompanyName(req.companyName().trim());
    a.setRegistrationNumber(req.registrationNumber().trim());
    a.setContactEmail(req.contactEmail().trim().toLowerCase());
    a.setContactPhone(req.contactPhone().trim());
    a.setStatus(CustomerStatus.ACTIVE);
    CorporateAccount saved = accounts.save(a);
    log.info("service=customer-service correlationId={} event=CORPORATE_CREATED accountId={} status=SUCCESS",
        correlationId(), saved.getId());
    return CustomerMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<CorporateAccountResponse> list() {
    return accounts.findAll().stream().map(CustomerMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public CorporateAccountResponse get(Long id) {
    return accounts.findById(id)
        .map(CustomerMapper::toResponse)
        .orElseThrow(() -> new NoSuchElementException("Corporate account not found: " + id));
  }

  private String correlationId() {
    return MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
  }
}
