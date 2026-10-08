package com.telecom.notification.service;

import com.telecom.notification.config.CorrelationIdFilter;
import com.telecom.notification.dto.CreateNotificationRequest;
import com.telecom.notification.dto.NotificationAttemptResponse;
import com.telecom.notification.dto.NotificationResponse;
import com.telecom.notification.dto.NotifyEventRequest;
import com.telecom.notification.dto.RenderPreviewRequest;
import com.telecom.notification.entity.Notification;
import com.telecom.notification.entity.NotificationAttempt;
import com.telecom.notification.entity.NotificationChannel;
import com.telecom.notification.entity.NotificationStatus;
import com.telecom.notification.entity.NotificationTemplate;
import com.telecom.notification.mapper.NotificationMapper;
import com.telecom.notification.repository.NotificationAttemptRepository;
import com.telecom.notification.repository.NotificationRepository;
import com.telecom.notification.repository.NotificationTemplateRepository;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simulated outbound notifications. Bodies render from templates with
 * {{placeholder}} substitution. Delivery simulation: recipients containing
 * "fail" deterministically fail (documented hook for retry/incident demos);
 * everything else "sends" with a fake provider message ID.
 */
@Service
public class NotificationService {

  private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

  private final NotificationRepository notifications;
  private final NotificationAttemptRepository attempts;
  private final NotificationTemplateRepository templates;

  public NotificationService(NotificationRepository notifications,
      NotificationAttemptRepository attempts, NotificationTemplateRepository templates) {
    this.notifications = notifications;
    this.attempts = attempts;
    this.templates = templates;
  }

  @Transactional
  public NotificationResponse create(CreateNotificationRequest req) {
    String subject = req.subject();
    String body = req.body();
    if (req.templateCode() != null) {
      NotificationTemplate t = getTemplate(req.templateCode());
      requireActive(t);
      subject = render(subject != null ? subject : t.getSubjectTemplate(), req.variables());
      body = render(body != null ? body : t.getBodyTemplate(), req.variables());
    }
    if (body == null || body.isBlank()) {
      throw new IllegalArgumentException("Notification needs a body or a template with one");
    }
    Notification n = new Notification();
    n.setNotificationNumber("NTF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
    n.setOrderId(req.orderId());
    n.setCustomerId(req.customerId());
    n.setChannel(req.channel());
    n.setRecipient(req.recipient().trim());
    n.setTemplateCode(req.templateCode());
    n.setSubject(subject);
    n.setBody(body);
    n.setStatus(NotificationStatus.PENDING);
    Notification saved = notifications.save(n);
    log("NOTIFICATION_QUEUED", saved);
    return NotificationMapper.toResponse(saved);
  }

  /** One call for order workflows: event code doubles as template code. */
  @Transactional
  public NotificationResponse notifyEvent(NotifyEventRequest req) {
    return create(new CreateNotificationRequest(req.orderId(), req.customerId(),
        req.channel(), req.recipient(), req.event(), null, null, req.variables()));
  }

  @Transactional(readOnly = true)
  public Page<NotificationResponse> list(NotificationStatus status, NotificationChannel channel, Pageable pageable) {
    Page<Notification> page;
    if (status != null && channel != null) page = notifications.findByStatusAndChannel(status, channel, pageable);
    else if (status != null) page = notifications.findByStatus(status, pageable);
    else if (channel != null) page = notifications.findByChannel(channel, pageable);
    else page = notifications.findAll(pageable);
    return page.map(NotificationMapper::toResponse);
  }

  @Transactional(readOnly = true)
  public NotificationResponse get(Long id) {
    return NotificationMapper.toResponse(getNotification(id));
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> byOrder(Long orderId) {
    return notifications.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
        .map(NotificationMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<NotificationResponse> byCustomer(Long customerId) {
    return notifications.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
        .map(NotificationMapper::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<NotificationAttemptResponse> attempts(Long id) {
    getNotification(id);
    return attempts.findByNotificationIdOrderByAttemptNoAsc(id).stream()
        .map(NotificationMapper::toResponse).toList();
  }

  @Transactional
  public NotificationResponse send(Long id) {
    Notification n = getNotification(id);
    if (n.getStatus() != NotificationStatus.PENDING && n.getStatus() != NotificationStatus.RETRYING) {
      throw new IllegalArgumentException("Only PENDING/RETRYING notifications can be sent (current: " + n.getStatus() + ")");
    }
    return deliver(n);
  }

  @Transactional
  public NotificationResponse retry(Long id) {
    Notification n = getNotification(id);
    if (n.getStatus() != NotificationStatus.FAILED) {
      throw new IllegalArgumentException("Only FAILED notifications can be retried (current: " + n.getStatus() + ")");
    }
    n.setStatus(NotificationStatus.RETRYING);
    return deliver(n);
  }

  @Transactional
  public NotificationResponse cancel(Long id) {
    Notification n = getNotification(id);
    if (n.getStatus() == NotificationStatus.SENT) {
      throw new IllegalArgumentException("SENT notifications cannot be cancelled");
    }
    n.setStatus(NotificationStatus.CANCELLED);
    log("NOTIFICATION_CANCELLED", n);
    return NotificationMapper.toResponse(n);
  }

  @Transactional
  public void delete(Long id) {
    Notification n = getNotification(id);
    if (n.getStatus() != NotificationStatus.PENDING && n.getStatus() != NotificationStatus.CANCELLED) {
      throw new IllegalArgumentException("Only PENDING/CANCELLED notifications can be deleted (current: " + n.getStatus() + ")");
    }
    attempts.deleteAll(attempts.findByNotificationIdOrderByAttemptNoAsc(id));
    notifications.delete(n);
    log.info("service=notification-service correlationId={} notificationId={} event=NOTIFICATION_DELETED status=SUCCESS",
        correlationId(), id);
  }

  @Transactional(readOnly = true)
  public String renderPreview(RenderPreviewRequest req) {
    NotificationTemplate t = getTemplate(req.templateCode());
    return render(t.getBodyTemplate(), req.variables());
  }

  private NotificationResponse deliver(Notification n) {
    n.setAttempts(n.getAttempts() + 1);
    NotificationAttempt a = new NotificationAttempt();
    a.setNotification(n);
    a.setAttemptNo(n.getAttempts());
    if (n.getRecipient().toLowerCase().contains("fail")) {
      n.setStatus(NotificationStatus.FAILED);
      n.setLastError("SIMULATED_PROVIDER_REJECT: recipient refused by downstream gateway");
      n.setProviderMessageId(null);
      a.setStatus(NotificationStatus.FAILED);
      a.setProviderResponse("550 recipient rejected (simulated)");
      attempts.save(a);
      log("NOTIFICATION_FAILED", n);
    } else {
      n.setStatus(NotificationStatus.SENT);
      n.setLastError(null);
      n.setProviderMessageId("SIM-PROV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
      a.setStatus(NotificationStatus.SENT);
      a.setProviderResponse("250 OK id=" + n.getProviderMessageId() + " (simulated)");
      attempts.save(a);
      log("NOTIFICATION_SENT", n);
    }
    return NotificationMapper.toResponse(n);
  }

  String render(String template, Map<String, String> variables) {
    if (template == null) return null;
    String out = template;
    if (variables != null) {
      for (Map.Entry<String, String> e : variables.entrySet()) {
        out = out.replace("{{" + e.getKey() + "}}", e.getValue() != null ? e.getValue() : "");
      }
    }
    return out;
  }

  private Notification getNotification(Long id) {
    return notifications.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Notification not found: " + id));
  }

  private NotificationTemplate getTemplate(String code) {
    return templates.findByTemplateCode(code)
        .orElseThrow(() -> new NoSuchElementException("Template not found: " + code));
  }

  private void requireActive(NotificationTemplate t) {
    if (!t.isActive()) {
      throw new IllegalArgumentException("Template is inactive: " + t.getTemplateCode());
    }
  }

  private void log(String event, Notification n) {
    log.info("service=notification-service correlationId={} notificationId={} orderId={} event={} status={}",
        correlationId(), n.getId(), n.getOrderId(), event, n.getStatus());
  }

  private String correlationId() {
    return MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
  }
}
