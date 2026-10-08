package com.telecom.notification.entity;

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

/** One simulated delivery attempt (provider response recorded). */
@Entity
@Table(name = "notification_attempt")
public class NotificationAttempt {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "notification_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attempt_notification"))
  private Notification notification;

  @Column(name = "attempt_no", nullable = false)
  private int attemptNo;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private NotificationStatus status;

  @Column(name = "provider_response", length = 1000)
  private String providerResponse;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  public Long getId() {
    return id;
  }

  public Notification getNotification() {
    return notification;
  }

  public void setNotification(Notification notification) {
    this.notification = notification;
  }

  public int getAttemptNo() {
    return attemptNo;
  }

  public void setAttemptNo(int attemptNo) {
    this.attemptNo = attemptNo;
  }

  public NotificationStatus getStatus() {
    return status;
  }

  public void setStatus(NotificationStatus status) {
    this.status = status;
  }

  public String getProviderResponse() {
    return providerResponse;
  }

  public void setProviderResponse(String providerResponse) {
    this.providerResponse = providerResponse;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
