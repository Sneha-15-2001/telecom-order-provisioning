package com.telecom.provisioning.config;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Dev-only fault injection, so upstream 5xx and 503 can be reproduced on demand
 * instead of only during a real outage.
 *
 * <p>Disabled unless {@code CHAOS_ENABLED=true}. It is deliberately awkward to
 * turn on: an accidental activation would look like a genuine outage, and
 * anything that can fabricate server errors should never be one env var away
 * from production.
 *
 * <p>Used by the platform-failure incident scenarios (502/503/500) to give
 * order-service a dependency that is broken on purpose.
 */
@Component
public class ChaosSwitch {

  private static final Logger log = LoggerFactory.getLogger(ChaosSwitch.class);

  /** An armed fault. Public so the filter can read it without reflection. */
  public record Fault(int status, long until, AtomicInteger hits) {
    public long expiresInSeconds() { return Math.max(0, (until - System.currentTimeMillis()) / 1000); }
  }

  private final boolean enabled;
  private final Map<String, Fault> faults = new ConcurrentHashMap<>();
  private final AtomicInteger requestCount = new AtomicInteger();

  public ChaosSwitch(@Value("${chaos.enabled:false}") boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  /** Arm a fault for {@code seconds}. Any non-2xx {@code status} may be injected. */
  public void arm(String path, int status, int seconds) {
    long until = System.currentTimeMillis() + seconds * 1000L;
    faults.put(path, new Fault(status, until, new AtomicInteger()));
    log.warn("event=CHAOS_ARMED path={} status={} seconds={}", path, status, seconds);
  }

  public void clear(String path) {
    if (path == null || path.isBlank() || "*".equals(path)) {
      faults.clear();
      log.warn("event=CHAOS_CLEARED scope=all");
    } else {
      faults.remove(path);
      log.warn("event=CHAOS_CLEARED path={}", path);
    }
  }

  /**
   * Active fault for this path, or null.
   *
   * <p>Every fault is counted so a scenario can prove the injection actually
   * fired rather than assuming it did.
   */
  public Fault activeFault(String path) {
    if (!enabled) {
      return null;
    }
    requestCount.incrementAndGet();
    Fault f = faults.get(path);
    if (f == null) {
      return null;
    }
    if (System.currentTimeMillis() > f.until()) {
      faults.remove(path);
      log.warn("event=CHAOS_EXPIRED path={}", path);
      return null;
    }
    f.hits().incrementAndGet();
    return f;
  }

  /** Snapshot for the scenario verification step. */
  public Map<String, Object> status() {
    long now = System.currentTimeMillis();
    Map<String, Object> out = new ConcurrentHashMap<>();
    out.put("enabled", enabled);
    out.put("requests", requestCount.get());
    faults.forEach((path, f) -> {
      if (now <= f.until()) {
        out.put(path, Map.of("status", f.status(), "hits", f.hits().get(),
            "expiresInSeconds", (f.until() - now) / 1000));
      }
    });
    return out;
  }
}
