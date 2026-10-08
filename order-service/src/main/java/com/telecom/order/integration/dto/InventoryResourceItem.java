package com.telecom.order.integration.dto;

/** Wire shape of inventory-service available resources (subset). */
public record InventoryResourceItem(Long id, String resourceNumber, String identifier, String status) {}
