package com.telecom.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.telecom.inventory.entity.InventoryResource;
import com.telecom.inventory.entity.ResourceStatus;
import com.telecom.inventory.entity.ResourceType;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class InventoryResourceRepositoryTest {

  @Autowired InventoryResourceRepository resources;

  @Test
  void persistsAndFindsByNumber() {
    InventoryResource r = new InventoryResource();
    r.setResourceNumber("RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    r.setResourceType(ResourceType.MSISDN);
    r.setIdentifier("91" + Math.abs(UUID.randomUUID().getMostSignificantBits() % 10000000000L));
    r.setStatus(ResourceStatus.AVAILABLE);
    resources.saveAndFlush(r);

    assertThat(resources.findByResourceNumber(r.getResourceNumber())).isPresent();
    assertThat(resources.findByIdentifier(r.getIdentifier())).isPresent();
  }
}
