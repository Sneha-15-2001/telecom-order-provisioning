package com.telecom.provisioning.service;

import com.telecom.provisioning.config.CorrelationIdFilter;
import com.telecom.provisioning.dto.CreateServiceProfileRequest;
import com.telecom.provisioning.dto.ServiceProfileResponse;
import com.telecom.provisioning.entity.ServiceProfile;
import com.telecom.provisioning.mapper.ProvisioningMapper;
import com.telecom.provisioning.repository.ServiceProfileRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reusable simulated service profiles. */
@Service
public class ServiceProfileService {

  private static final Logger log = LoggerFactory.getLogger(ServiceProfileService.class);

  private final ServiceProfileRepository profiles;

  public ServiceProfileService(ServiceProfileRepository profiles) {
    this.profiles = profiles;
  }

  @Transactional
  public ServiceProfileResponse create(CreateServiceProfileRequest req) {
    profiles.findByProfileCode(req.profileCode().trim().toUpperCase()).ifPresent(p -> {
      throw new IllegalArgumentException("Profile already exists: " + req.profileCode());
    });
    ServiceProfile p = new ServiceProfile();
    p.setProfileCode(req.profileCode().trim().toUpperCase());
    p.setServiceType(req.serviceType());
    p.setDescription(req.description().trim());
    p.setConfig(req.config());
    p.setActive(req.active());
    ServiceProfile saved = profiles.save(p);
    log.info("service=provisioning-service correlationId={} event=PROFILE_CREATED profile={} status=SUCCESS",
        MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY), saved.getProfileCode());
    return ProvisioningMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<ServiceProfileResponse> list() {
    return profiles.findAll().stream().map(ProvisioningMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ServiceProfileResponse get(Long id) {
    return profiles.findById(id)
        .map(ProvisioningMapper::toResponse)
        .orElseThrow(() -> new NoSuchElementException("Service profile not found: " + id));
  }
}
