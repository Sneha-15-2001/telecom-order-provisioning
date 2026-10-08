package com.telecom.provisioning.service;

import com.telecom.provisioning.config.CorrelationIdFilter;
import com.telecom.provisioning.dto.CreateProvisioningRequest;
import com.telecom.provisioning.dto.ProvisioningHistoryEntry;
import com.telecom.provisioning.dto.ProvisioningResponse;
import com.telecom.provisioning.entity.ProvisioningHistory;
import com.telecom.provisioning.entity.ProvisioningRequest;
import com.telecom.provisioning.entity.ProvisioningStatus;
import com.telecom.provisioning.entity.ServiceType;
import com.telecom.provisioning.mapper.ProvisioningMapper;
import com.telecom.provisioning.repository.ProvisioningHistoryRepository;
import com.telecom.provisioning.repository.ProvisioningRequestRepository;
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
 * Simulated provisioning lifecycle. Activation succeeds deterministically when
 * the identifiers required by the service type are present, otherwise the
 * request FAILS with a lastError (used by incident scenarios in Phase 13).
 */
@Service
public class ProvisioningService {

  private static final Logger log = LoggerFactory.getLogger(ProvisioningService.class);

  private final ProvisioningRequestRepository requests;
  private final ProvisioningHistoryRepository history;

  public ProvisioningService(ProvisioningRequestRepository requests,
      ProvisioningHistoryRepository history) {
    this.requests = requests;
    this.history = history;
  }

  @Transactional
  public ProvisioningResponse create(CreateProvisioningRequest req) {
    ProvisioningRequest r = new ProvisioningRequest();
    r.setRequestNumber("PRV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    r.setOrderId(req.orderId());
    r.setCustomerId(req.customerId());
    r.setServiceType(req.serviceType());
    r.setMsisdn(req.msisdn());
    r.setResourceNumber(req.resourceNumber());
    r.setPlanCode(req.planCode());
    r.setStatus(ProvisioningStatus.PENDING);
    ProvisioningRequest saved = requests.save(r);
    record(saved, "PROVISIONING_CREATED", null, ProvisioningStatus.PENDING,
        "service " + req.serviceType() + " for order " + req.orderId());
    log("PROVISIONING_CREATED", saved);
    return ProvisioningMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public Page<ProvisioningResponse> list(ServiceType serviceType, ProvisioningStatus status, Pageable pageable) {
    Page<ProvisioningRequest> page;
    if (serviceType != null && status != null) page = requests.findByServiceTypeAndStatus(serviceType, status, pageable);
    else if (serviceType != null) page = requests.findByServiceType(serviceType, pageable);
    else if (status != null) page = requests.findByStatus(status, pageable);
    else page = requests.findAll(pageable);
    return page.map(ProvisioningMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public List<ProvisioningResponse> byOrder(Long orderId) {
    return requests.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
        .map(ProvisioningMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ProvisioningResponse get(Long id) {
    return ProvisioningMapper.toResponse(getRequest(id));
  }

  @Transactional(readOnly = true)
  public List<ProvisioningHistoryEntry> history(Long id) {
    getRequest(id);
    return history.findByRequestIdOrderByCreatedAtAscIdAsc(id).stream()
        .map(h -> new ProvisioningHistoryEntry(h.getId(), h.getEvent(), h.getFromStatus(),
            h.getToStatus(), h.getComment(), h.getCreatedAt()))
        .toList();
  }

  @Transactional
  public ProvisioningResponse start(Long id) {
    ProvisioningRequest r = getRequest(id);
    if (r.getStatus() != ProvisioningStatus.PENDING) {
      throw new IllegalArgumentException("Only PENDING requests can start (current: " + r.getStatus() + ")");
    }
    transition(r, ProvisioningStatus.IN_PROGRESS, "simulation started");
    log("PROVISIONING_STARTED", r);
    return ProvisioningMapper.toResponse(r);
  }

  @Transactional
  public ProvisioningResponse activate(Long id) {
    ProvisioningRequest r = getRequest(id);
    if (r.getStatus() != ProvisioningStatus.IN_PROGRESS) {
      throw new IllegalArgumentException("Only IN_PROGRESS requests can activate (current: " + r.getStatus() + ")");
    }
    r.setAttempts(r.getAttempts() + 1);
    String missing = missingIdentifier(r);
    if (missing != null) {
      r.setLastError(missing);
      transition(r, ProvisioningStatus.FAILED, missing);
      log("PROVISIONING_FAILED", r);
    } else {
      r.setLastError(null);
      transition(r, ProvisioningStatus.COMPLETED, "simulated activation succeeded");
      log("PROVISIONING_COMPLETED", r);
    }
    return ProvisioningMapper.toResponse(r);
  }

  @Transactional
  public ProvisioningResponse deactivate(Long id) {
    ProvisioningRequest r = getRequest(id);
    if (r.getStatus() != ProvisioningStatus.COMPLETED) {
      throw new IllegalArgumentException("Only COMPLETED services can be deactivated (current: " + r.getStatus() + ")");
    }
    transition(r, ProvisioningStatus.CANCELLED, "service deactivated by request");
    log("SERVICE_DEACTIVATED", r);
    return ProvisioningMapper.toResponse(r);
  }

  @Transactional
  public ProvisioningResponse retry(Long id) {
    ProvisioningRequest r = getRequest(id);
    if (r.getStatus() != ProvisioningStatus.FAILED) {
      throw new IllegalArgumentException("Only FAILED requests can be retried (current: " + r.getStatus() + ")");
    }
    transition(r, ProvisioningStatus.PENDING, "retry requested (attempt " + (r.getAttempts() + 1) + ")");
    log("PROVISIONING_RETRY", r);
    return ProvisioningMapper.toResponse(r);
  }

  @Transactional
  public ProvisioningResponse reprocess(Long id) {
    ProvisioningRequest r = getRequest(id);
    if (r.getStatus() != ProvisioningStatus.FAILED && r.getStatus() != ProvisioningStatus.CANCELLED) {
      throw new IllegalArgumentException("Only FAILED/CANCELLED requests can be reprocessed (current: " + r.getStatus() + ")");
    }
    r.setLastError(null);
    transition(r, ProvisioningStatus.PENDING, "reprocessed from scratch");
    log("PROVISIONING_REPROCESSED", r);
    return ProvisioningMapper.toResponse(r);
  }

  @Transactional
  public ProvisioningResponse cancel(Long id) {
    ProvisioningRequest r = getRequest(id);
    if (r.getStatus() == ProvisioningStatus.COMPLETED || r.getStatus() == ProvisioningStatus.ROLLED_BACK) {
      throw new IllegalArgumentException("Request cannot be cancelled from " + r.getStatus());
    }
    transition(r, ProvisioningStatus.CANCELLED, "cancelled by request");
    log("PROVISIONING_CANCELLED", r);
    return ProvisioningMapper.toResponse(r);
  }

  @Transactional
  public ProvisioningResponse rollback(Long id) {
    ProvisioningRequest r = getRequest(id);
    if (r.getStatus() != ProvisioningStatus.FAILED && r.getStatus() != ProvisioningStatus.IN_PROGRESS) {
      throw new IllegalArgumentException("Only FAILED/IN_PROGRESS requests can be rolled back (current: " + r.getStatus() + ")");
    }
    transition(r, ProvisioningStatus.ROLLED_BACK, "simulated rollback completed");
    log("PROVISIONING_ROLLED_BACK", r);
    return ProvisioningMapper.toResponse(r);
  }

  /** Pre-flight check of required identifiers per service type (no state change). */
  @Transactional(readOnly = true)
  public ProvisioningResponse validate(Long id) {
    ProvisioningRequest r = getRequest(id);
    String missing = missingIdentifier(r);
    if (missing != null) {
      throw new IllegalArgumentException("Provisioning request is not activatable: " + missing);
    }
    log.info("service=provisioning-service correlationId={} requestId={} orderId={} event=PROVISIONING_VALIDATED status={}",
        MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY), r.getId(), r.getOrderId(), r.getStatus());
    return ProvisioningMapper.toResponse(r);
  }

  /**
   * Deterministic simulation rule: MOBILE/ESIM/ROAMING need an MSISDN,
   * BROADBAND/FIBER need a resource number, DEVICE needs either.
   */
  String missingIdentifier(ProvisioningRequest r) {
    return switch (r.getServiceType()) {
      case MOBILE, ESIM, ROAMING ->
          r.getMsisdn() == null ? "MISSING_MSISDN: " + r.getServiceType() + " activation requires an MSISDN" : null;
      case BROADBAND, FIBER ->
          r.getResourceNumber() == null ? "MISSING_RESOURCE: " + r.getServiceType() + " activation requires a resource number" : null;
      case DEVICE ->
          (r.getMsisdn() == null && r.getResourceNumber() == null)
              ? "MISSING_IDENTIFIER: DEVICE activation requires an MSISDN or resource number" : null;
    };
  }

  private ProvisioningRequest getRequest(Long id) {
    return requests.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Provisioning request not found: " + id));
  }

  private void transition(ProvisioningRequest r, ProvisioningStatus to, String comment) {
    ProvisioningStatus from = r.getStatus();
    r.setStatus(to);
    record(r, "STATUS_CHANGED", from, to, comment);
  }

  private void record(ProvisioningRequest r, String event, ProvisioningStatus from,
      ProvisioningStatus to, String comment) {
    ProvisioningHistory h = new ProvisioningHistory();
    h.setRequest(r);
    h.setEvent(event);
    h.setFromStatus(from);
    h.setToStatus(to);
    h.setComment(comment);
    history.save(h);
  }

  private void log(String event, ProvisioningRequest r) {
    log.info("service=provisioning-service correlationId={} requestId={} orderId={} event={} status={}",
        MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY),
        r.getId(), r.getOrderId(), event, r.getStatus());
  }
}
