package com.telecom.provisioning.repository;

import com.telecom.provisioning.entity.ServiceProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceProfileRepository extends JpaRepository<ServiceProfile, Long> {

  Optional<ServiceProfile> findByProfileCode(String profileCode);
}
