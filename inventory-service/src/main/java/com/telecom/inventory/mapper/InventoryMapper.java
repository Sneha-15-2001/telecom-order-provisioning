package com.telecom.inventory.mapper;

import com.telecom.inventory.dto.ReservationResponse;
import com.telecom.inventory.dto.ResourceResponse;
import com.telecom.inventory.entity.InventoryResource;
import com.telecom.inventory.entity.Reservation;

/** Explicit entity → DTO mapping. */
public final class InventoryMapper {

  private InventoryMapper() {}

  public static ResourceResponse toResponse(InventoryResource r) {
    return new ResourceResponse(r.getId(), r.getResourceNumber(), r.getResourceType(),
        r.getIdentifier(), r.getStatus(), r.getDetails(), r.getOrderId(),
        r.getCreatedAt(), r.getUpdatedAt());
  }

  public static ReservationResponse toResponse(Reservation s) {
    return new ReservationResponse(s.getId(), s.getReservationNumber(),
        s.getResource().getId(), s.getResource().getResourceNumber(),
        s.getOrderId(), s.getCustomerId(), s.getStatus(),
        s.getReservedAt(), s.getExpiresAt(), s.getConfirmedAt());
  }
}
