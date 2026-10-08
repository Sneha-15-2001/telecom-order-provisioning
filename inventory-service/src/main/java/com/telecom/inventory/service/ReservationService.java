package com.telecom.inventory.service;

import com.telecom.inventory.config.CorrelationIdFilter;
import com.telecom.inventory.dto.ReservationResponse;
import com.telecom.inventory.dto.ReserveRequest;
import com.telecom.inventory.entity.InventoryResource;
import com.telecom.inventory.entity.Reservation;
import com.telecom.inventory.entity.ReservationStatus;
import com.telecom.inventory.entity.ResourceStatus;
import com.telecom.inventory.mapper.InventoryMapper;
import com.telecom.inventory.repository.ReservationRepository;
import java.time.LocalDateTime;
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

/** Reserve → confirm (allocate) → release/cancel lifecycle for order holds. */
@Service
public class ReservationService {

  private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

  private static final int DEFAULT_TTL_MINUTES = 30;

  private final ReservationRepository reservations;
  private final InventoryService inventory;

  public ReservationService(ReservationRepository reservations, InventoryService inventory) {
    this.reservations = reservations;
    this.inventory = inventory;
  }

  @Transactional
  public ReservationResponse reserve(ReserveRequest req) {
    InventoryResource r = inventory.getResource(req.resourceId());
    if (r.getStatus() != ResourceStatus.AVAILABLE) {
      throw new IllegalArgumentException(
          "Only AVAILABLE resources can be reserved (current: " + r.getStatus() + ")");
    }
    Reservation s = new Reservation();
    s.setReservationNumber("RSV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    s.setResource(r);
    s.setOrderId(req.orderId());
    s.setCustomerId(req.customerId());
    s.setStatus(ReservationStatus.ACTIVE);
    int ttl = req.ttlMinutes() != null ? req.ttlMinutes() : DEFAULT_TTL_MINUTES;
    s.setExpiresAt(LocalDateTime.now().plusMinutes(ttl));
    Reservation saved = reservations.save(s);
    r.setOrderId(req.orderId());
    inventory.setStatus(r, ResourceStatus.RESERVED, "RESOURCE_RESERVED",
        "reservation " + saved.getReservationNumber() + " for order " + req.orderId());
    return InventoryMapper.toResponse(saved);
  }

  /** Confirm a hold: resource becomes ALLOCATED to the order. */
  @Transactional
  public ReservationResponse confirm(Long id) {
    Reservation s = getReservation(id);
    requireActive(s);
    s.setStatus(ReservationStatus.CONFIRMED);
    s.setConfirmedAt(LocalDateTime.now());
    inventory.setStatus(s.getResource(), ResourceStatus.ALLOCATED, "RESOURCE_ALLOCATED",
        "reservation " + s.getReservationNumber() + " confirmed for order " + s.getOrderId());
    return InventoryMapper.toResponse(s);
  }

  /** Backwards-compatible alias used by order orchestration (Phase 7/10). */
  @Transactional
  public ReservationResponse allocate(Long id) {
    return confirm(id);
  }

  /** Release a hold: reservation cancelled, resource freed (if still reserved). */
  @Transactional
  public ReservationResponse release(Long id) {
    Reservation s = getReservation(id);
    requireActive(s);
    s.setStatus(ReservationStatus.CANCELLED);
    InventoryResource r = s.getResource();
    if (r.getStatus() == ResourceStatus.RESERVED) {
      r.setOrderId(null);
      inventory.setStatus(r, ResourceStatus.AVAILABLE, "RESOURCE_RELEASED",
          "reservation " + s.getReservationNumber() + " released");
    }
    log("RESERVATION_CANCELLED", s);
    return InventoryMapper.toResponse(s);
  }

  @Transactional(readOnly = true)
  public Page<ReservationResponse> list(ReservationStatus status, Pageable pageable) {
    Page<Reservation> page = status == null ? reservations.findAll(pageable)
        : reservations.findByStatus(status, pageable);
    return page.map(InventoryMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public ReservationResponse get(Long id) {
    return InventoryMapper.toResponse(getReservation(id));
  }

  @Transactional(readOnly = true)
  public List<ReservationResponse> byOrder(Long orderId) {
    return reservations.findByOrderIdOrderByReservedAtDesc(orderId).stream()
        .map(InventoryMapper::toResponse).toList();
  }

  private Reservation getReservation(Long id) {
    return reservations.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Reservation not found: " + id));
  }

  private void requireActive(Reservation s) {
    if (s.getStatus() != ReservationStatus.ACTIVE) {
      throw new IllegalArgumentException(
          "Only ACTIVE reservations can be changed (current: " + s.getStatus() + ")");
    }
    if (s.getExpiresAt().isBefore(LocalDateTime.now())) {
      s.setStatus(ReservationStatus.EXPIRED);
      throw new IllegalArgumentException("Reservation expired: " + s.getReservationNumber());
    }
  }

  private void log(String event, Reservation s) {
    log.info("service=inventory-service correlationId={} reservationId={} orderId={} event={} status={}",
        MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY),
        s.getId(), s.getOrderId(), event, s.getStatus());
  }
}
