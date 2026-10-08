package com.telecom.inventory.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.inventory.dto.CreateResourceRequest;
import com.telecom.inventory.dto.ReservationResponse;
import com.telecom.inventory.dto.ReserveRequest;
import com.telecom.inventory.dto.ResourceResponse;
import com.telecom.inventory.entity.ReservationStatus;
import com.telecom.inventory.entity.ResourceStatus;
import com.telecom.inventory.entity.ResourceType;
import com.telecom.inventory.service.InventoryService;
import com.telecom.inventory.service.ReservationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({InventoryController.class, ReservationController.class})
class InventoryControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean InventoryService inventoryService;
  @MockBean ReservationService reservationService;

  @Test
  void createReturns201() throws Exception {
    var res = new ResourceResponse(1L, "RES-ABC123", ResourceType.SIM, "899100000000000001",
        ResourceStatus.AVAILABLE, null, null, LocalDateTime.now(), LocalDateTime.now());
    when(inventoryService.create(any())).thenReturn(res);

    mvc.perform(post("/api/inventory/resources")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(
                new CreateResourceRequest(ResourceType.SIM, "899100000000000001", null))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.resourceNumber").value("RES-ABC123"));
  }

  @Test
  void reserveReturns201() throws Exception {
    var res = new ReservationResponse(1L, "RSV-ABC123", 1L, "RES-ABC123", 9L, 1L,
        ReservationStatus.ACTIVE, LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), null);
    when(reservationService.reserve(any())).thenReturn(res);

    mvc.perform(post("/api/inventory/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ReserveRequest(1L, 9L, 1L, 30))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.reservationNumber").value("RSV-ABC123"));
  }

  @Test
  void getMissingReturns404() throws Exception {
    when(inventoryService.getById(eq(999L)))
        .thenThrow(new java.util.NoSuchElementException("Resource not found: 999"));

    mvc.perform(get("/api/inventory/resources/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("NOT_FOUND"));
  }
}
