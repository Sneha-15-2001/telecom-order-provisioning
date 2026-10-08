package com.telecom.provisioning.repository;

import com.telecom.provisioning.entity.ProvisioningRequest;
import com.telecom.provisioning.entity.ProvisioningStatus;
import com.telecom.provisioning.entity.ServiceType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvisioningRequestRepository extends JpaRepository<ProvisioningRequest, Long> {

  Optional<ProvisioningRequest> findByRequestNumber(String requestNumber);

  List<ProvisioningRequest> findByOrderIdOrderByCreatedAtDesc(Long orderId);

  Page<ProvisioningRequest> findByStatus(ProvisioningStatus status, Pageable pageable);

  Page<ProvisioningRequest> findByServiceType(ServiceType serviceType, Pageable pageable);

  Page<ProvisioningRequest> findByServiceTypeAndStatus(ServiceType serviceType, ProvisioningStatus status, Pageable pageable);
}
