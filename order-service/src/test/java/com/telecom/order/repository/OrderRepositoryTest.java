package com.telecom.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.telecom.order.entity.CustomerOrder;
import com.telecom.order.entity.OrderPriority;
import com.telecom.order.entity.OrderStatus;
import com.telecom.order.entity.OrderType;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OrderRepositoryTest {

  @Autowired OrderRepository orders;

  @Test
  void persistsAndFindsByNumber() {
    CustomerOrder o = new CustomerOrder();
    o.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    o.setCustomerId(999L);
    o.setOrderType(OrderType.DEVICE_ONLY);
    o.setStatus(OrderStatus.CREATED);
    o.setPriority(OrderPriority.HIGH);
    o.setTotalAmount(new BigDecimal("1499.00"));
    o.setDiscountAmount(BigDecimal.ZERO);
    orders.saveAndFlush(o);

    assertThat(orders.findByOrderNumber(o.getOrderNumber())).isPresent();

    Page<CustomerOrder> page = orders.search(o.getOrderNumber().toLowerCase(), PageRequest.of(0, 10));
    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
  }
}
