package com.telecom.order.controller;

import com.telecom.order.dto.ProductResponse;
import com.telecom.order.entity.OrderItemType;
import com.telecom.order.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Sellable catalogue, so order entry picks products instead of typing codes. */
@RestController
@RequestMapping(path = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Products", description = "Sellable catalogue")
public class ProductController {

  private final ProductService products;

  public ProductController(ProductService products) {
    this.products = products;
  }

  @Operation(summary = "List active products, optionally filtered by item type")
  @GetMapping
  public List<ProductResponse> list(@RequestParam(required = false) OrderItemType itemType) {
    return products.list(itemType);
  }
}