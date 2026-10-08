package com.telecom.provisioning.mapper;

import com.telecom.provisioning.dto.ProvisioningResponse;
import com.telecom.provisioning.dto.ServiceProfileResponse;
import com.telecom.provisioning.entity.ProvisioningRequest;
import com.telecom.provisioning.entity.ServiceProfile;

/** Explicit entity → DTO mapping. */
public final class ProvisioningMapper {

  private ProvisioningMapper() {}

  public static ProvisioningResponse toResponse(ProvisioningRequest r) {
    return new ProvisioningResponse(r.getId(), r.getRequestNumber(), r.getOrderId(),
        r.getCustomerId(), r.getServiceType(), r.getMsisdn(), r.getResourceNumber(),
        r.getPlanCode(), r.getStatus(), r.getAttempts(), r.getLastError(),
        r.getCreatedAt(), r.getUpdatedAt());
  }

  public static ServiceProfileResponse toResponse(ServiceProfile p) {
    return new ServiceProfileResponse(p.getId(), p.getProfileCode(), p.getServiceType(),
        p.getDescription(), p.getConfig(), p.isActive());
  }
}
