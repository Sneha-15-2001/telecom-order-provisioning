package com.telecom.provisioning.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.telecom.provisioning.entity.ProvisioningRequest;
import com.telecom.provisioning.entity.ProvisioningStatus;
import com.telecom.provisioning.entity.ServiceType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProvisioningRequestRepositoryTest {

  @Autowired ProvisioningRequestRepository requests;

  @Test
  void persistsAndFindsByNumber() {
    ProvisioningRequest r = new ProvisioningRequest();
    r.setRequestNumber("PRV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    r.setOrderId(999L);
    r.setServiceType(ServiceType.DEVICE);
    r.setResourceNumber("RES-DEV0001");
    r.setStatus(ProvisioningStatus.PENDING);
    requests.saveAndFlush(r);

    assertThat(requests.findByRequestNumber(r.getRequestNumber())).isPresent();
  }
}
