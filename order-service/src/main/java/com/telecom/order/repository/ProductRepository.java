package com.telecom.order.repository;

import com.telecom.order.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

  List<Product> findByActiveTrueOrderByItemTypeAscNameAsc();

  List<Product> findByActiveTrueAndItemTypeOrderByNameAsc(com.telecom.order.entity.OrderItemType itemType);

  Optional<Product> findByProductCode(String productCode);
}