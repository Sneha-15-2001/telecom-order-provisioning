package com.telecom.inventory.repository;

import com.telecom.inventory.entity.InventoryResource;
import com.telecom.inventory.entity.ResourceStatus;
import com.telecom.inventory.entity.ResourceType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryResourceRepository extends JpaRepository<InventoryResource, Long> {

  Optional<InventoryResource> findByResourceNumber(String resourceNumber);

  Optional<InventoryResource> findByIdentifier(String identifier);

  Page<InventoryResource> findByResourceType(ResourceType type, Pageable pageable);

  Page<InventoryResource> findByStatus(ResourceStatus status, Pageable pageable);

  Page<InventoryResource> findByResourceTypeAndStatus(ResourceType type, ResourceStatus status, Pageable pageable);

  List<InventoryResource> findTop50ByResourceTypeAndStatusOrderByIdAsc(ResourceType type, ResourceStatus status);

  @Query(
      "SELECT r FROM InventoryResource r WHERE "
          + "LOWER(r.resourceNumber) LIKE LOWER(CONCAT('%', :q, '%')) OR "
          + "LOWER(r.identifier) LIKE LOWER(CONCAT('%', :q, '%')) OR "
          + "LOWER(COALESCE(r.details, '')) LIKE LOWER(CONCAT('%', :q, '%'))")
  Page<InventoryResource> search(@Param("q") String query, Pageable pageable);
}
