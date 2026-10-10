package com.telecom.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sellable product in the catalogue.
 *
 * <p>Order entry previously asked an operator to type a product code and a
 * unit price by hand. That is not how a telco sells: the price is catalogue
 * data owned by pricing, not something an agent invents at the counter. This
 * table is the catalogue those prices come from, so the UI picks a product and
 * the server still decides the amount.
 */
@Entity
@Table(
    name = "product",
    uniqueConstraints = @UniqueConstraint(name = "uk_product_code", columnNames = "product_code"))
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "product_code", nullable = false, updatable = false, length = 50)
  private String productCode;

  @Column(name = "name", nullable = false, length = 200)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "item_type", nullable = false, length = 30)
  private OrderItemType itemType;

  /** Monthly recurring charge in paise-precise decimal, exclusive of tax. */
  @Column(name = "price", nullable = false, precision = 12, scale = 2)
  private BigDecimal price;

  /** One-line description shown to the customer when they choose this plan. */
  @Column(name = "description", length = 500)
  private String description;

  /** Monthly data allowance in GB; null for non-data products. */
  @Column(name = "data_gb")
  private Integer dataGb;

  @Column(name = "active", nullable = false)
  private boolean active;

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

  public String getProductCode() {
    return productCode;
  }

  public void setProductCode(String productCode) {
    this.productCode = productCode;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OrderItemType getItemType() {
    return itemType;
  }

  public void setItemType(OrderItemType itemType) {
    this.itemType = itemType;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getDataGb() {
    return dataGb;
  }

  public void setDataGb(Integer dataGb) {
    this.dataGb = dataGb;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}