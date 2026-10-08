package com.telecom.inventory.repository;

import com.telecom.inventory.entity.InventoryHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

  List<InventoryHistory> findByResourceIdOrderByCreatedAtAscIdAsc(Long resourceId);
}
