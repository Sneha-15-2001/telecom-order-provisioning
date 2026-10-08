package com.telecom.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A telecom resource: SIM/eSIM/MSISDN/device/IMEI/fiber-port/ONT/router/profile.
 * {@code identifier} holds the natural key (ICCID, MSISDN, IMEI, port ID…).
 */
@Entity
@Table(
    name = "inventory_resource",
    uniqueConstraints = {
      @UniqueConstraint(name = "uk_resource_number", columnNames = "resource_number"),
      @UniqueConstraint(name = "uk_resource_identifier", columnNames = "identifier")
    })
public class InventoryResource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "resource_number", nullable = false, updatable = false, length = 20)
  private String resourceNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "resource_type", nullable = false, length = 20)
  private ResourceType resourceType;

  @Column(name = "identifier", nullable = false, length = 100)
  private String identifier;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ResourceStatus status;

  @Column(name = "details", length = 1000)
  private String details;

  @Column(name = "order_id")
  private Long orderId;

  @OneToMany(mappedBy = "resource")
  private List<Reservation> reservations = new ArrayList<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  void onCreate() {
    LocalDateTime now = LocalDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public String getResourceNumber() {
    return resourceNumber;
  }

  public void setResourceNumber(String resourceNumber) {
    this.resourceNumber = resourceNumber;
  }

  public ResourceType getResourceType() {
    return resourceType;
  }

  public void setResourceType(ResourceType resourceType) {
    this.resourceType = resourceType;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public ResourceStatus getStatus() {
    return status;
  }

  public void setStatus(ResourceStatus status) {
    this.status = status;
  }

  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }

  public Long getOrderId() {
    return orderId;
  }

  public void setOrderId(Long orderId) {
    this.orderId = orderId;
  }

  public List<Reservation> getReservations() {
    return reservations;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
