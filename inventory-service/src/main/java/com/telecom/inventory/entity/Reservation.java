package com.telecom.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

/** A hold on a resource for a specific order (expires unless confirmed). */
@Entity
@Table(
    name = "resource_reservation",
    uniqueConstraints = @UniqueConstraint(name = "uk_reservation_number", columnNames = "reservation_number"))
public class Reservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "reservation_number", nullable = false, updatable = false, length = 20)
  private String reservationNumber;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "resource_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reservation_resource"))
  private InventoryResource resource;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "customer_id")
  private Long customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ReservationStatus status;

  @Column(name = "reserved_at", nullable = false)
  private LocalDateTime reservedAt;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "confirmed_at")
  private LocalDateTime confirmedAt;

  @PrePersist
  void onCreate() {
    this.reservedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getReservationNumber() {
    return reservationNumber;
  }

  public void setReservationNumber(String reservationNumber) {
    this.reservationNumber = reservationNumber;
  }

  public InventoryResource getResource() {
    return resource;
  }

  public void setResource(InventoryResource resource) {
    this.resource = resource;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public Long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(Long customerId) {
    this.customerId = customerId;
  }

  public ReservationStatus getStatus() {
    return status;
  }

  public void setStatus(ReservationStatus status) {
    this.status = status;
  }

  public LocalDateTime getReservedAt() {
    return reservedAt;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(LocalDateTime expiresAt) {
    this.expiresAt = expiresAt;
  }

  public LocalDateTime getConfirmedAt() {
    return confirmedAt;
  }

  public void setConfirmedAt(LocalDateTime confirmedAt) {
    this.confirmedAt = confirmedAt;
  }
}
