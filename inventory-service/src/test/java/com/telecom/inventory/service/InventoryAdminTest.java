package com.telecom.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.telecom.inventory.dto.ReconcileRequest;
import com.telecom.inventory.entity.InventoryResource;
import com.telecom.inventory.entity.ResourceStatus;
import com.telecom.inventory.entity.ResourceType;
import com.telecom.inventory.repository.InventoryHistoryRepository;
import com.telecom.inventory.repository.InventoryResourceRepository;
import com.telecom.inventory.repository.ReservationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryAdminTest {

  @Mock InventoryResourceRepository resources;
  @Mock InventoryHistoryRepository history;
  @Mock ReservationRepository reservations;

  @InjectMocks InventoryService service;

  private InventoryResource resource(ResourceStatus status) {
    InventoryResource r = new InventoryResource();
    r.setResourceType(ResourceType.SIM);
    r.setIdentifier("X");
    r.setStatus(status);
    r.setOrderId(9L);
    return r;
  }

  @Test
  void reconcileCorrectsStatusAndClearsOrder() {
    when(resources.findById(1L)).thenReturn(Optional.of(resource(ResourceStatus.ALLOCATED)));

    var res = service.reconcile(new ReconcileRequest(1L, ResourceStatus.AVAILABLE, "audit says free"));
    assertThat(res.status()).isEqualTo(ResourceStatus.AVAILABLE);
    assertThat(res.orderId()).isNull();
  }

  @Test
  void deleteNonAvailableRejected() {
    when(resources.findById(2L)).thenReturn(Optional.of(resource(ResourceStatus.ALLOCATED)));

    assertThatThrownBy(() -> service.delete(2L)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void quarantineRequiresAvailableOrReserved() {
    when(resources.findById(3L)).thenReturn(Optional.of(resource(ResourceStatus.QUARANTINED)));

    assertThatThrownBy(() -> service.quarantine(3L, "x")).isInstanceOf(IllegalArgumentException.class);
  }
}
