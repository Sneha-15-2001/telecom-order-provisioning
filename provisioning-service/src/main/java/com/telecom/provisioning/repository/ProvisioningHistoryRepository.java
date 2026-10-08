package com.telecom.provisioning.repository;

import com.telecom.provisioning.entity.ProvisioningHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvisioningHistoryRepository extends JpaRepository<ProvisioningHistory, Long> {

  List<ProvisioningHistory> findByRequestIdOrderByCreatedAtAscIdAsc(Long requestId);
}
