package com.telecom.customer.controller;

import com.telecom.customer.dto.AddressRequest;
import com.telecom.customer.dto.AddressResponse;
import com.telecom.customer.dto.CreateCustomerRequest;
import com.telecom.customer.dto.CustomerHistoryResponse;
import com.telecom.customer.dto.CustomerResponse;
import com.telecom.customer.dto.CustomerStatusUpdateRequest;
import com.telecom.customer.dto.CustomerValidationResponse;
import com.telecom.customer.dto.EligibilityResponse;
import com.telecom.customer.dto.SubscriptionRequest;
import com.telecom.customer.dto.SubscriptionResponse;
import com.telecom.customer.dto.SubscriptionStatusUpdateRequest;
import com.telecom.customer.dto.UpdateCustomerRequest;
import com.telecom.customer.entity.CustomerStatus;
import com.telecom.customer.service.CustomerService;
import com.telecom.customer.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Customer onboarding, profile, status, eligibility, history, addresses, subscriptions. */
@Tag(name = "Customers", description = "Customer & subscription management (Phase 2)")
@RestController
@RequestMapping(path = "/api/customers", produces = MediaType.APPLICATION_JSON_VALUE)
public class CustomerController {

  private final CustomerService customerService;
  private final SubscriptionService subscriptionService;

  public CustomerController(CustomerService customerService, SubscriptionService subscriptionService) {
    this.customerService = customerService;
    this.subscriptionService = subscriptionService;
  }

  @Operation(summary = "Onboard a new customer")
  @ApiResponse(responseCode = "201", description = "Customer created")
  @ApiResponse(responseCode = "400", description = "Validation failed or email/phone already registered")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public CustomerResponse create(@Valid @RequestBody CreateCustomerRequest request) {
    return customerService.create(request);
  }

  @Operation(summary = "List customers (paged, optional status filter)")
  @GetMapping
  public Page<CustomerResponse> list(
      @RequestParam(required = false) CustomerStatus status, @ParameterObject Pageable pageable) {
    return customerService.list(status, pageable);
  }

  @Operation(summary = "Search customers by name, email, phone or customer number")
  @GetMapping("/search")
  public Page<CustomerResponse> search(@RequestParam String q, @ParameterObject Pageable pageable) {
    return customerService.search(q, pageable);
  }

  @Operation(summary = "Get a customer by ID")
  @ApiResponse(responseCode = "200", description = "Customer found")
  @ApiResponse(responseCode = "404", description = "Customer not found")
  @GetMapping("/{id}")
  public CustomerResponse getById(@PathVariable Long id) {
    return customerService.getById(id);
  }

  @Operation(summary = "Get a customer by customer number")
  @GetMapping("/number/{customerNumber}")
  public CustomerResponse getByNumber(@PathVariable String customerNumber) {
    return customerService.getByNumber(customerNumber);
  }

  @Operation(summary = "Update a customer profile")
  @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public CustomerResponse update(@PathVariable Long id, @Valid @RequestBody UpdateCustomerRequest request) {
    return customerService.update(id, request);
  }

  @Operation(summary = "Delete a customer (with addresses and subscriptions)")
  @ApiResponse(responseCode = "204", description = "Customer deleted")
  @ApiResponse(responseCode = "404", description = "Customer not found")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    customerService.delete(id);
  }

  @Operation(summary = "Change customer status (admin override)")
  @PatchMapping(path = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
  public CustomerResponse updateStatus(@PathVariable Long id, @Valid @RequestBody CustomerStatusUpdateRequest request) {
    return customerService.updateStatus(id, request.status());
  }

  @Operation(summary = "Suspend a customer (ACTIVE → SUSPENDED)")
  @PostMapping("/{id}/suspend")
  public CustomerResponse suspend(@PathVariable Long id) {
    return customerService.suspend(id);
  }

  @Operation(summary = "Reactivate a customer (SUSPENDED/INACTIVE → ACTIVE)")
  @PostMapping("/{id}/reactivate")
  public CustomerResponse reactivate(@PathVariable Long id) {
    return customerService.reactivate(id);
  }

  @Operation(summary = "Validate a customer for order placement (order-service contract)")
  @ApiResponse(responseCode = "200", description = "Validation result (valid flag carries the verdict)")
  @ApiResponse(responseCode = "404", description = "Customer not found")
  @PostMapping("/{id}/validate")
  public CustomerValidationResponse validate(@PathVariable Long id) {
    return customerService.validate(id);
  }

  @Operation(summary = "Check whether the customer may buy new services")
  @GetMapping("/{id}/eligibility")
  public EligibilityResponse eligibility(@PathVariable Long id) {
    return customerService.eligibility(id);
  }

  @Operation(summary = "Customer timeline (created + subscription events)")
  @GetMapping("/{id}/history")
  public CustomerHistoryResponse history(@PathVariable Long id) {
    return customerService.history(id);
  }

  @Operation(summary = "List customer addresses")
  @GetMapping("/{id}/addresses")
  public List<AddressResponse> listAddresses(@PathVariable Long id) {
    return customerService.listAddresses(id);
  }

  @Operation(summary = "Add an address to a customer")
  @PostMapping(path = "/{id}/addresses", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public AddressResponse addAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
    return customerService.addAddress(id, request);
  }

  @Operation(summary = "List customer subscriptions")
  @GetMapping("/{id}/subscriptions")
  public List<SubscriptionResponse> listSubscriptions(@PathVariable Long id) {
    return subscriptionService.list(id);
  }

  @Operation(summary = "Add a subscription to a customer")
  @PostMapping(path = "/{id}/subscriptions", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public SubscriptionResponse addSubscription(@PathVariable Long id, @Valid @RequestBody SubscriptionRequest request) {
    return subscriptionService.create(id, request);
  }

  @Operation(summary = "Change a subscription status")
  @PutMapping(path = "/{id}/subscriptions/{subscriptionId}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public SubscriptionResponse updateSubscription(
      @PathVariable Long id, @PathVariable Long subscriptionId,
      @Valid @RequestBody SubscriptionStatusUpdateRequest request) {
    return subscriptionService.updateStatus(id, subscriptionId, request);
  }

  @Operation(summary = "Cancel a subscription")
  @PostMapping("/{id}/subscriptions/{subscriptionId}/cancel")
  public SubscriptionResponse cancelSubscription(@PathVariable Long id, @PathVariable Long subscriptionId) {
    return subscriptionService.cancel(id, subscriptionId);
  }
}
