package com.telecom.notification.service;

import com.telecom.notification.config.CorrelationIdFilter;
import com.telecom.notification.dto.CreateTemplateRequest;
import com.telecom.notification.dto.TemplateResponse;
import com.telecom.notification.entity.NotificationTemplate;
import com.telecom.notification.mapper.NotificationMapper;
import com.telecom.notification.repository.NotificationTemplateRepository;
import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Message template catalog. */
@Service
public class TemplateService {

  private static final Logger log = LoggerFactory.getLogger(TemplateService.class);

  private final NotificationTemplateRepository templates;

  public TemplateService(NotificationTemplateRepository templates) {
    this.templates = templates;
  }

  @Transactional
  public TemplateResponse create(CreateTemplateRequest req) {
    templates.findByTemplateCode(req.templateCode().trim().toUpperCase()).ifPresent(t -> {
      throw new IllegalArgumentException("Template already exists: " + req.templateCode());
    });
    NotificationTemplate t = new NotificationTemplate();
    t.setTemplateCode(req.templateCode().trim().toUpperCase());
    t.setChannel(req.channel());
    t.setSubjectTemplate(req.subjectTemplate());
    t.setBodyTemplate(req.bodyTemplate());
    t.setActive(req.active());
    NotificationTemplate saved = templates.save(t);
    log.info("service=notification-service correlationId={} event=TEMPLATE_CREATED template={} status=SUCCESS",
        MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY), saved.getTemplateCode());
    return NotificationMapper.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<TemplateResponse> list() {
    return templates.findAll().stream().map(NotificationMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public TemplateResponse get(Long id) {
    return templates.findById(id)
        .map(NotificationMapper::toResponse)
        .orElseThrow(() -> new NoSuchElementException("Template not found: " + id));
  }

  @Transactional(readOnly = true)
  public TemplateResponse getByCode(String code) {
    return templates.findByTemplateCode(code)
        .map(NotificationMapper::toResponse)
        .orElseThrow(() -> new NoSuchElementException("Template not found: " + code));
  }

  @Transactional
  public TemplateResponse setActive(Long id, boolean active) {
    NotificationTemplate t = templates.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Template not found: " + id));
    t.setActive(active);
    return NotificationMapper.toResponse(t);
  }
}
