package com.telecom.provisioning.entity;

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
 * A simulated network/service provisioning request for an order.
 * NEVER touches real network elements — activation is a deterministic
 * simulation (required identifiers present → success, else FAILED).
 */
@Entity
@Table(
    name = "provisioning_request",
    uniqueConstraints = @UniqueConstraint(name = "uk_request_number", columnNames = "request_number"))
public class ProvisioningRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "request_number", nullable = false, updatable = false, length = 20)
  private String requestNumber;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "customer_id")
  private Long customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "service_type", nullable = false, length = 20)
  private ServiceType serviceType;

  @Column(name = "msisdn", length = 15)
  private String msisdn;

  @Column(name = "resource_number", length = 20)
  private String resourceNumber;

  @Column(name = "plan_code", length = 50)
  private String planCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ProvisioningStatus status;

  @Column(name = "attempts", nullable = false)
  private int attempts;

  @Column(name = "last_error", length = 1000)
  private String lastError;

  @OneToMany(mappedBy = "request")
  private List<ProvisioningHistory> history = new ArrayList<>();

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

  public String getRequestNumber() {
    return requestNumber;
  }

  public void setRequestNumber(String requestNumber) {
    this.requestNumber = requestNumber;
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

  public ServiceType getServiceType() {
    return serviceType;
  }

  public void setServiceType(ServiceType serviceType) {
    this.serviceType = serviceType;
  }

  public String getMsisdn() {
    return msisdn;
  }

  public void setMsisdn(String msisdn) {
    this.msisdn = msisdn;
  }

  public String getResourceNumber() {
    return resourceNumber;
  }

  public void setResourceNumber(String resourceNumber) {
    this.resourceNumber = resourceNumber;
  }

  public String getPlanCode() {
    return planCode;
  }

  public void setPlanCode(String planCode) {
    this.planCode = planCode;
  }

  public ProvisioningStatus getStatus() {
    return status;
  }

  public void setStatus(ProvisioningStatus status) {
    this.status = status;
  }

  public int getAttempts() {
    return attempts;
  }

  public void setAttempts(int attempts) {
    this.attempts = attempts;
  }

  public String getLastError() {
    return lastError;
  }

  public void setLastError(String lastError) {
    this.lastError = lastError;
  }

  public List<ProvisioningHistory> getHistory() {
    return history;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
