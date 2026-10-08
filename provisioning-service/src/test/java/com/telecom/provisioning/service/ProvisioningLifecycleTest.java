package com.telecom.provisioning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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
class ProvisioningLifecycleTest {

  @Mock ProvisioningRequestRepository requests;
  @Mock ProvisioningHistoryRepository history;

  @InjectMocks ProvisioningService service;

  private ProvisioningRequest request(ProvisioningStatus status) {
    ProvisioningRequest r = new ProvisioningRequest();
    r.setStatus(status);
    r.setServiceType(ServiceType.MOBILE);
    r.setMsisdn("919876543210");
    r.setAttempts(1);
    return r;
  }

  @Test
  void retryResetsToPending() {
    when(requests.findById(1L)).thenReturn(Optional.of(request(ProvisioningStatus.FAILED)));
    assertThat(service.retry(1L).status()).isEqualTo(ProvisioningStatus.PENDING);
  }

  @Test
  void rollbackFromInProgress() {
    when(requests.findById(2L)).thenReturn(Optional.of(request(ProvisioningStatus.IN_PROGRESS)));
    assertThat(service.rollback(2L).status()).isEqualTo(ProvisioningStatus.ROLLED_BACK);
  }

  @Test
  void rollbackRejectedFromCompleted() {
    when(requests.findById(3L)).thenReturn(Optional.of(request(ProvisioningStatus.COMPLETED)));
    assertThatThrownBy(() -> service.rollback(3L)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void deactivateCompleted() {
    when(requests.findById(4L)).thenReturn(Optional.of(request(ProvisioningStatus.COMPLETED)));
    assertThat(service.deactivate(4L).status()).isEqualTo(ProvisioningStatus.CANCELLED);
  }

  @Test
  void reprocessFailed() {
    when(requests.findById(5L)).thenReturn(Optional.of(request(ProvisioningStatus.FAILED)));
    assertThat(service.reprocess(5L).status()).isEqualTo(ProvisioningStatus.PENDING);
  }

  @Test
  void validateRejectsMissingMsisdn() {
    ProvisioningRequest r = request(ProvisioningStatus.PENDING);
    r.setMsisdn(null);
    when(requests.findById(6L)).thenReturn(Optional.of(r));
    assertThatThrownBy(() -> service.validate(6L)).isInstanceOf(IllegalArgumentException.class);
  }
}
