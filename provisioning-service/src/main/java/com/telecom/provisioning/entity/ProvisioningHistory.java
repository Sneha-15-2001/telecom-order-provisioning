package com.telecom.provisioning.entity;

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
import java.time.LocalDateTime;

/** Immutable audit trail for a provisioning request. */
@Entity
@Table(name = "provisioning_history")
public class ProvisioningHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "request_id", nullable = false, foreignKey = @ForeignKey(name = "fk_history_request"))
  private ProvisioningRequest request;

  @Column(name = "event", nullable = false, length = 50)
  private String event;

  @Enumerated(EnumType.STRING)
  @Column(name = "from_status", length = 20)
  private ProvisioningStatus fromStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "to_status", length = 20)
  private ProvisioningStatus toStatus;

  @Column(name = "comment", length = 1000)
  private String comment;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public ProvisioningRequest getRequest() {
    return request;
  }

  public void setRequest(ProvisioningRequest request) {
    this.request = request;
  }

  public String getEvent() {
    return event;
  }

  public void setEvent(String event) {
    this.event = event;
  }

  public ProvisioningStatus getFromStatus() {
    return fromStatus;
  }

  public void setFromStatus(ProvisioningStatus fromStatus) {
    this.fromStatus = fromStatus;
  }

  public ProvisioningStatus getToStatus() {
    return toStatus;
  }

  public void setToStatus(ProvisioningStatus toStatus) {
    this.toStatus = toStatus;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
