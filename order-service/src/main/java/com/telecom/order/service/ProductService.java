package com.telecom.order.service;

import com.telecom.order.dto.ProductResponse;
import com.telecom.order.entity.OrderItemType;
import com.telecom.order.entity.Product;
import com.telecom.order.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

  private final ProductRepository products;

  public ProductService(ProductRepository products) {
    this.products = products;
  }

  @Transactional(readOnly = true)
  public List<ProductResponse> list(OrderItemType itemType) {
    List<Product> found = itemType == null
        ? products.findByActiveTrueOrderByItemTypeAscNameAsc()
        : products.findByActiveTrueAndItemTypeOrderByNameAsc(itemType);
    return found.stream().map(ProductService::toResponse).toList();
  }

  static ProductResponse toResponse(Product p) {
    return new ProductResponse(p.getId(), p.getProductCode(), p.getName(), p.getItemType(),
        p.getPrice(), p.getDescription(), p.getDataGb(), p.isActive());
  }
}