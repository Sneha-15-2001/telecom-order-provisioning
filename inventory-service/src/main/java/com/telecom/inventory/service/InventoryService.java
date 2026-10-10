package com.telecom.inventory.service;

import com.telecom.inventory.config.CorrelationIdFilter;
import com.telecom.inventory.dto.CreateResourceRequest;
import com.telecom.inventory.dto.ReconcileRequest;
import com.telecom.inventory.dto.ResourceHistoryEntry;
import com.telecom.inventory.dto.ResourceResponse;
import com.telecom.inventory.dto.UpdateResourceRequest;
import com.telecom.inventory.exception.StateConflictException;
import com.telecom.inventory.entity.InventoryHistory;
import com.telecom.inventory.entity.InventoryResource;
import com.telecom.inventory.entity.ResourceStatus;
import com.telecom.inventory.entity.ResourceType;
import com.telecom.inventory.mapper.InventoryMapper;
import com.telecom.inventory.repository.InventoryHistoryRepository;
import com.telecom.inventory.repository.InventoryResourceRepository;
import com.telecom.inventory.repository.ReservationRepository;
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

/** Resource catalog: register, search, availability, quarantine, reconcile, history. */
@Service
public class InventoryService {

  private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

  private final InventoryResourceRepository resources;
  private final InventoryHistoryRepository history;
  private final ReservationRepository reservations;

  public InventoryService(InventoryResourceRepository resources,
      InventoryHistoryRepository history, ReservationRepository reservations) {
    this.resources = resources;
    this.history = history;
    this.reservations = reservations;
  }

  @Transactional
  public ResourceResponse create(CreateResourceRequest req) {
    resources.findByIdentifier(req.identifier().trim()).ifPresent(r -> {
      throw new IllegalArgumentException("Identifier already registered: " + req.identifier());
    });
    InventoryResource r = new InventoryResource();
    r.setResourceNumber("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    r.setResourceType(req.resourceType());
    r.setIdentifier(req.identifier().trim());
    r.setDetails(req.details());
    r.setStatus(ResourceStatus.AVAILABLE);
    InventoryResource saved = resources.save(r);
    record(saved, "RESOURCE_REGISTERED", null, ResourceStatus.AVAILABLE, "type " + req.resourceType());
    log("RESOURCE_REGISTERED", saved);
    return InventoryMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public Page<ResourceResponse> list(ResourceType type, ResourceStatus status, Pageable pageable) {
    Page<InventoryResource> page;
    if (type != null && status != null) page = resources.findByResourceTypeAndStatus(type, status, pageable);
    else if (type != null) page = resources.findByResourceType(type, pageable);
    else if (status != null) page = resources.findByStatus(status, pageable);
    else page = resources.findAll(pageable);
    return page.map(InventoryMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public Page<ResourceResponse> search(String q, Pageable pageable) {
    return resources.search(q, pageable).map(InventoryMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public List<ResourceResponse> available(ResourceType type, int limit) {
    return resources.findTop50ByResourceTypeAndStatusOrderByIdAsc(type, ResourceStatus.AVAILABLE).stream()
        .limit(limit > 0 ? limit : 50)
        .map(InventoryMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ResourceResponse getById(Long id) {
    return InventoryMapper.toResponse(getResource(id));
  }

  @Transactional(readOnly = true)
  public ResourceResponse getByNumber(String resourceNumber) {
    return resources.findByResourceNumber(resourceNumber)
        .map(InventoryMapper::toResponse)
        .orElseThrow(() -> new NoSuchElementException("Resource not found: " + resourceNumber));
  }

  @Transactional
  public ResourceResponse update(Long id, UpdateResourceRequest req) {
    InventoryResource r = getResource(id);
    if (req.details() != null) r.setDetails(req.details());
    record(r, "RESOURCE_UPDATED", r.getStatus(), r.getStatus(), "details updated");
    return InventoryMapper.toResponse(r);
  }

  /**
   * Test-data cleanup: removes an AVAILABLE resource together with its
   * reservations and history rows. Production decommissioning should use a
   * status change (e.g. reconcile to DECOMMISSIONED) to preserve the audit trail.
   */
  @Transactional
  public void delete(Long id) {
    InventoryResource r = getResource(id);
    if (r.getStatus() != ResourceStatus.AVAILABLE) {
      throw new StateConflictException("RESOURCE_STATE_INVALID", "Only AVAILABLE resources can be deleted (current: " + r.getStatus() + ")");
    }
    reservations.deleteAll(reservations.findByResourceId(id));
    history.deleteAll(history.findByResourceIdOrderByCreatedAtAscIdAsc(id));
    resources.delete(r);
    log.info("service=inventory-service correlationId={} resourceId={} event=RESOURCE_DELETED status=SUCCESS",
        correlationId(), id);
  }

  @Transactional
  public ResourceResponse quarantine(Long id, String reason) {
    InventoryResource r = getResource(id);
    if (r.getStatus() != ResourceStatus.AVAILABLE && r.getStatus() != ResourceStatus.RESERVED) {
      throw new StateConflictException("RESOURCE_STATE_INVALID", "Only AVAILABLE/RESERVED resources can be quarantined (current: " + r.getStatus() + ")");
    }
    ResourceStatus from = r.getStatus();
    r.setStatus(ResourceStatus.QUARANTINED);
    record(r, "RESOURCE_QUARANTINED", from, ResourceStatus.QUARANTINED, reason);
    log("RESOURCE_QUARANTINED", r);
    return InventoryMapper.toResponse(r);
  }

  @Transactional
  public ResourceResponse releaseQuarantine(Long id, String reason) {
    InventoryResource r = getResource(id);
    if (r.getStatus() != ResourceStatus.QUARANTINED) {
      throw new StateConflictException("RESOURCE_STATE_INVALID", "Only QUARANTINED resources can be released (current: " + r.getStatus() + ")");
    }
    r.setStatus(ResourceStatus.AVAILABLE);
    record(r, "QUARANTINE_RELEASED", ResourceStatus.QUARANTINED, ResourceStatus.AVAILABLE, reason);
    log("QUARANTINE_RELEASED", r);
    return InventoryMapper.toResponse(r);
  }

  /**
   * Admin correction of a wrong status (e.g. resource shows ALLOCATED but the
   * network says it is free). Every correction is audited in history.
   */
  @Transactional
  public ResourceResponse reconcile(ReconcileRequest req) {
    InventoryResource r = getResource(req.resourceId());
    ResourceStatus from = r.getStatus();
    r.setStatus(req.actualStatus());
    if (req.actualStatus() == ResourceStatus.AVAILABLE) {
      r.setOrderId(null);
    }
    record(r, "RESOURCE_RECONCILED", from, req.actualStatus(), req.reason());
    log("RESOURCE_RECONCILED", r);
    return InventoryMapper.toResponse(r);
  }

  @Transactional(readOnly = true)
  public List<ResourceHistoryEntry> history(Long id) {
    getResource(id);
    return history.findByResourceIdOrderByCreatedAtAscIdAsc(id).stream()
        .map(h -> new ResourceHistoryEntry(h.getId(), h.getEvent(), h.getFromStatus(),
            h.getToStatus(), h.getComment(), h.getCreatedAt()))
        .toList();
  }

  InventoryResource getResource(Long id) {
    return resources.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Resource not found: " + id));
  }

  void record(InventoryResource r, String event, ResourceStatus from, ResourceStatus to, String comment) {
    InventoryHistory h = new InventoryHistory();
    h.setResource(r);
    h.setEvent(event);
    h.setFromStatus(from);
    h.setToStatus(to);
    h.setComment(comment);
    history.save(h);
  }

  void setStatus(InventoryResource r, ResourceStatus to, String event, String comment) {
    ResourceStatus from = r.getStatus();
    r.setStatus(to);
    record(r, event, from, to, comment);
    log(event, r);
  }

  private void log(String event, InventoryResource r) {
    log.info("service=inventory-service correlationId={} resourceId={} orderId={} event={} status={}",
        correlationId(), r.getId(), r.getOrderId(), event, r.getStatus());
  }

  private String correlationId() {
    return MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
  }
}
