package com.telecom.customer.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.customer.dto.CreateCustomerRequest;
import com.telecom.customer.dto.CustomerResponse;
import com.telecom.customer.dto.CustomerValidationResponse;
import com.telecom.customer.entity.CustomerStatus;
import com.telecom.customer.entity.CustomerType;
import com.telecom.customer.service.CorporateAccountService;
import com.telecom.customer.service.CustomerService;
import com.telecom.customer.service.SubscriptionService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({CustomerController.class, CorporateAccountController.class})
class CustomerControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean CustomerService customerService;
  @MockBean SubscriptionService subscriptionService;
  @MockBean CorporateAccountService corporateAccountService;

  private CustomerResponse sample() {
    return new CustomerResponse(1L, "CUS-ABC123", "Aarav", "Sharma", "a@x.com",
        "+919876543210", null, CustomerType.INDIVIDUAL, CustomerStatus.ACTIVE,
        null, LocalDateTime.now(), LocalDateTime.now());
  }

  @Test
  void createReturns201() throws Exception {
    when(customerService.create(any())).thenReturn(sample());
    var req = new CreateCustomerRequest("Aarav", "Sharma", "a@x.com",
        "+919876543210", null, CustomerType.INDIVIDUAL, null);

    mvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.customerNumber").value("CUS-ABC123"));
  }

  @Test
  void createRejectsInvalidEmailWith400() throws Exception {
    var req = new CreateCustomerRequest("Aarav", "Sharma", "not-an-email",
        "+919876543210", null, CustomerType.INDIVIDUAL, null);

    mvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
  }

  @Test
  void validateEndpointReturnsContract() throws Exception {
    when(customerService.validate(eq(1L))).thenReturn(
        new CustomerValidationResponse(1L, "CUS-ABC123", CustomerStatus.ACTIVE, true, List.of()));

    mvc.perform(post("/api/customers/1/validate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.valid").value(true));
  }
}
