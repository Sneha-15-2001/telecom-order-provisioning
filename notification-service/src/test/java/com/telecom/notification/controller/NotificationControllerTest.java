package com.telecom.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telecom.notification.dto.CreateNotificationRequest;
import com.telecom.notification.dto.NotificationResponse;
import com.telecom.notification.entity.NotificationChannel;
import com.telecom.notification.entity.NotificationStatus;
import com.telecom.notification.service.NotificationService;
import com.telecom.notification.service.TemplateService;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({NotificationController.class, TemplateController.class})
class NotificationControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean NotificationService notificationService;
  @MockBean TemplateService templateService;

  private NotificationResponse sample(NotificationStatus status) {
    return new NotificationResponse(1L, "NTF-ABC123", 2L, 1L, NotificationChannel.SMS,
        "+919876543210", "ORDER_CREATED", null, "order ORD-1 is CREATED", status, 1,
        null, "SIM-PROV-XYZ", LocalDateTime.now(), LocalDateTime.now());
  }

  @Test
  void createReturns201() throws Exception {
    when(notificationService.create(any())).thenReturn(sample(NotificationStatus.PENDING));

    mvc.perform(post("/api/notifications")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CreateNotificationRequest(2L, 1L,
                NotificationChannel.SMS, "+919876543210", "ORDER_CREATED", null, null,
                Map.of("orderNumber", "ORD-1", "status", "CREATED", "planName", "5G 299")))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.notificationNumber").value("NTF-ABC123"));
  }

  @Test
  void sendReturnsSent() throws Exception {
    when(notificationService.send(eq(1L))).thenReturn(sample(NotificationStatus.SENT));

    mvc.perform(post("/api/notifications/1/send"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SENT"));
  }

  @Test
  void getMissingReturns404() throws Exception {
    when(notificationService.get(eq(999L)))
        .thenThrow(new java.util.NoSuchElementException("Notification not found: 999"));

    mvc.perform(get("/api/notifications/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("NOT_FOUND"));
  }
}
