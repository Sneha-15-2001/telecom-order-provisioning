package com.telecom.provisioning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.telecom.provisioning.dto.CreateProvisioningRequest;
import com.telecom.provisioning.dto.ProvisioningResponse;
import com.telecom.provisioning.entity.ProvisioningRequest;
import com.telecom.provisioning.entity.ProvisioningStatus;
import com.telecom.provisioning.entity.ServiceType;
import com.telecom.provisioning.repository.ProvisioningHistoryRepository;
import com.telecom.provisioning.repository.ProvisioningRequestRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProvisioningServiceTest {

  @Mock ProvisioningRequestRepository requests;
  @Mock ProvisioningHistoryRepository history;

  @InjectMocks ProvisioningService service;

  private ProvisioningRequest request(ProvisioningStatus status, ServiceType type, String msisdn, String resource) {
    ProvisioningRequest r = new ProvisioningRequest();
    r.setStatus(status);
    r.setServiceType(type);
    r.setMsisdn(msisdn);
    r.setResourceNumber(resource);
    r.setAttempts(0);
    return r;
  }

  @Test
  void activateSucceedsWithMsisdn() {
    ProvisioningRequest r = request(ProvisioningStatus.IN_PROGRESS, ServiceType.MOBILE, "919876543210", null);
    when(requests.findById(1L)).thenReturn(Optional.of(r));

    ProvisioningResponse res = service.activate(1L);
    assertThat(res.status()).isEqualTo(ProvisioningStatus.COMPLETED);
    assertThat(res.attempts()).isEqualTo(1);
  }

  @Test
  void activateFailsWithoutRequiredIdentifier() {
    ProvisioningRequest r = request(ProvisioningStatus.IN_PROGRESS, ServiceType.BROADBAND, null, null);
    when(requests.findById(2L)).thenReturn(Optional.of(r));

    ProvisioningResponse res = service.activate(2L);
    assertThat(res.status()).isEqualTo(ProvisioningStatus.FAILED);
    assertThat(res.lastError()).contains("MISSING_RESOURCE");
  }

  @Test
  void startRequiresPending() {
    ProvisioningRequest r = request(ProvisioningStatus.COMPLETED, ServiceType.MOBILE, "919876543210", null);
    when(requests.findById(3L)).thenReturn(Optional.of(r));

    assertThatThrownBy(() -> service.start(3L)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void createOpensPending() {
    when(requests.save(any(ProvisioningRequest.class))).thenAnswer(i -> i.getArgument(0));

    ProvisioningResponse res = service.create(
        new CreateProvisioningRequest(2L, 1L, ServiceType.ESIM, "919876543210", null, "PLAN_5G_299"));
    assertThat(res.requestNumber()).startsWith("PRV-");
    assertThat(res.status()).isEqualTo(ProvisioningStatus.PENDING);
  }
}
