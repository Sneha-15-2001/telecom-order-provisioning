package com.telecom.provisioning.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.provisioning.dto.CreateProvisioningRequest;
import com.telecom.provisioning.dto.ProvisioningResponse;
import com.telecom.provisioning.entity.ProvisioningStatus;
import com.telecom.provisioning.entity.ServiceType;
import com.telecom.provisioning.service.ProvisioningService;
import com.telecom.provisioning.service.ServiceProfileService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({ProvisioningController.class, ServiceProfileController.class})
class ProvisioningControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean ProvisioningService provisioningService;
  @MockBean ServiceProfileService profileService;

  private ProvisioningResponse sample(ProvisioningStatus status) {
    return new ProvisioningResponse(1L, "PRV-ABC123", 2L, 1L, ServiceType.MOBILE,
        "919876543210", null, "PLAN_5G_299", status, 1, null,
        LocalDateTime.now(), LocalDateTime.now());
  }

  @Test
  void createReturns201() throws Exception {
    when(provisioningService.create(any())).thenReturn(sample(ProvisioningStatus.PENDING));

    mvc.perform(post("/api/provisioning")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new CreateProvisioningRequest(2L, 1L, ServiceType.MOBILE, "919876543210", null, "PLAN_5G_299"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.requestNumber").value("PRV-ABC123"));
  }

  @Test
  void activateReturnsCompleted() throws Exception {
    when(provisioningService.activate(eq(1L))).thenReturn(sample(ProvisioningStatus.COMPLETED));

    mvc.perform(post("/api/provisioning/1/activate"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  void validateRejectsBadRequest() throws Exception {
    when(provisioningService.validate(eq(9L)))
        .thenThrow(new IllegalArgumentException("Provisioning request is not activatable: MISSING_MSISDN"));

    mvc.perform(post("/api/provisioning/9/validate"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
  }

  @Test
  void getMissingReturns404() throws Exception {
    when(provisioningService.get(eq(999L)))
        .thenThrow(new java.util.NoSuchElementException("Provisioning request not found: 999"));

    mvc.perform(get("/api/provisioning/999"))
        .andExpect(status().isNotFound());
  }
}
