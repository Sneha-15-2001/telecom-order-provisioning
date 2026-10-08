package com.telecom.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.telecom.inventory.dto.CreateResourceRequest;
import com.telecom.inventory.dto.ReserveRequest;
import com.telecom.inventory.dto.ResourceResponse;
import com.telecom.inventory.entity.InventoryResource;
import com.telecom.inventory.entity.Reservation;
import com.telecom.inventory.entity.ReservationStatus;
import com.telecom.inventory.entity.ResourceStatus;
import com.telecom.inventory.entity.ResourceType;
import com.telecom.inventory.repository.InventoryHistoryRepository;
import com.telecom.inventory.repository.InventoryResourceRepository;
import com.telecom.inventory.repository.ReservationRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

  @Mock InventoryResourceRepository resources;
  @Mock InventoryHistoryRepository history;
  @Mock ReservationRepository reservations;

  @InjectMocks InventoryService inventoryService;

  ReservationService reservationService;

  @BeforeEach
  void setUp() {
    reservationService = new ReservationService(reservations, inventoryService);
  }

  private InventoryResource resource(ResourceStatus status) {
    InventoryResource r = new InventoryResource();
    r.setResourceType(ResourceType.SIM);
    r.setIdentifier("899100000000000099");
    r.setStatus(status);
    return r;
  }

  @Test
  void createRegistersAsAvailable() {
    when(resources.findByIdentifier("899100000000000099")).thenReturn(Optional.empty());
    when(resources.save(any(InventoryResource.class))).thenAnswer(i -> i.getArgument(0));

    ResourceResponse res = inventoryService.create(
        new CreateResourceRequest(ResourceType.SIM, "899100000000000099", null));
    assertThat(res.resourceNumber()).startsWith("RES-");
    assertThat(res.status()).isEqualTo(ResourceStatus.AVAILABLE);
  }

  @Test
  void reserveRequiresAvailable() {
    when(resources.findById(1L)).thenReturn(Optional.of(resource(ResourceStatus.ALLOCATED)));

    assertThatThrownBy(() -> reservationService.reserve(new ReserveRequest(1L, 9L, 1L, 30)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("AVAILABLE");
  }

  @Test
  void reserveConfirmReleaseFlow() {
    InventoryResource r = resource(ResourceStatus.AVAILABLE);
    when(resources.findById(2L)).thenReturn(Optional.of(r));
    when(reservations.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

    var created = reservationService.reserve(new ReserveRequest(2L, 9L, 1L, 30));
    assertThat(r.getStatus()).isEqualTo(ResourceStatus.RESERVED);

    Reservation s = new Reservation();
    s.setResource(r);
    s.setOrderId(9L);
    s.setStatus(ReservationStatus.ACTIVE);
    s.setExpiresAt(LocalDateTime.now().plusMinutes(30));
    when(reservations.findById(7L)).thenReturn(Optional.of(s));

    assertThat(reservationService.confirm(7L).status()).isEqualTo(ReservationStatus.CONFIRMED);
    assertThat(r.getStatus()).isEqualTo(ResourceStatus.ALLOCATED);
  }

  @Test
  void quarantineAndRelease() {
    InventoryResource r = resource(ResourceStatus.AVAILABLE);
    when(resources.findById(3L)).thenReturn(Optional.of(r));

    assertThat(inventoryService.quarantine(3L, "QA hold").status()).isEqualTo(ResourceStatus.QUARANTINED);
    assertThat(inventoryService.releaseQuarantine(3L, "cleared").status()).isEqualTo(ResourceStatus.AVAILABLE);
  }
}
