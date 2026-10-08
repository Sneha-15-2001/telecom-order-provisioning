package com.telecom.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.telecom.customer.entity.Customer;
import com.telecom.customer.entity.CustomerStatus;
import com.telecom.customer.entity.CustomerType;
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
class CustomerRepositoryTest {

  @Autowired CustomerRepository customers;

  @Test
  void persistsAndFindsByEmailAndSearches() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    Customer c = new Customer();
    c.setCustomerNumber("CUS-" + suffix.toUpperCase());
    c.setFirstName("Test");
    c.setLastName("User" + suffix);
    c.setEmail("test." + suffix + "@example.com");
    c.setPhone("+910000" + suffix.replace("-", "").substring(0, 6));
    c.setCustomerType(CustomerType.INDIVIDUAL);
    c.setStatus(CustomerStatus.ACTIVE);
    customers.saveAndFlush(c);

    assertThat(customers.findByEmail(c.getEmail())).isPresent();
    assertThat(customers.findByCustomerNumber(c.getCustomerNumber())).isPresent();

    Page<Customer> page = customers.search(suffix.toLowerCase(), PageRequest.of(0, 10));
    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
  }
}
