package com.telecom.order.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Fixed-window rate limit, returning 429 when a caller exceeds its budget.
 *
 * <p>Order entry is the write-heavy surface: a misbehaving integration or a
 * double-clicking agent can create orders far faster than a human would. A real
 * BSS rate-limits order intake rather than letting a runaway client fill the
 * database or exhaust the number pool.
 *
 * <p>Fixed window per client key, in memory, per instance. That is honest for a
 * single node and is explicitly not a distributed limit — a multi-instance
 * deployment would need a shared counter (Redis) or a gateway-level limit. The
 * in-memory map is swept lazily so a flood of distinct keys cannot grow it
 * without bound.
 *
 * <p>The 429 carries {@code Retry-After} and {@code X-RateLimit-*} headers so a
 * client can back off correctly rather than guessing.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

  private final int maxRequests;
  private final long windowSeconds;

  private record Window(AtomicInteger count, AtomicLong windowStart) {}

  private final Map<String, Window> buckets = new ConcurrentHashMap<>();
  private volatile long lastSweep = System.currentTimeMillis();

  public RateLimitFilter(
      @Value("${app.rate-limit.max-requests:120}") int maxRequests,
      @Value("${app.rate-limit.window-seconds:60}") long windowSeconds) {
    this.maxRequests = maxRequests;
    this.windowSeconds = windowSeconds;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    // Health and docs must never be throttled — a monitoring probe hitting 429
    // would report the service as down when it is fine.
    String path = request.getRequestURI();
    if (path.startsWith("/actuator") || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-ui")) {
      chain.doFilter(request, response);
      return;
    }

    sweep();
    String key = clientKey(request);
    Window bucket = buckets.computeIfAbsent(key, k -> new Window(new AtomicInteger(), new AtomicLong(now())));
    long now = now();
    long start = bucket.windowStart.get();
    if (now - start >= windowSeconds * 1000L && bucket.windowStart.compareAndSet(start, now)) {
      bucket.count.set(0);
    }
    int used = bucket.count.incrementAndGet();

    response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
    response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxRequests - used)));

    if (used > maxRequests) {
      long retryAfter = Math.max(1, windowSeconds - (now - bucket.windowStart.get()) / 1000);
      response.setStatus(429);
      response.setContentType("application/json");
      response.setHeader("Retry-After", String.valueOf(retryAfter));
      String cid = MDC.get("correlationId");
      response.getWriter().write(
          "{\"timestamp\":\"" + java.time.Instant.now() + "\","
              + "\"status\":429,\"error\":\"RATE_LIMIT_EXCEEDED\","
              + "\"message\":\"Too many requests. Limit is " + maxRequests + " per "
              + windowSeconds + "s; retry in " + retryAfter + "s.\","
              + "\"path\":\"" + path + "\",\"correlationId\":\"" + (cid == null ? "" : cid) + "\"}");
      return;
    }
    chain.doFilter(request, response);
  }

  /** Behind a proxy the remote address is the proxy; prefer the forwarded header. */
  private String clientKey(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  /** Drop windows that can no longer be hit. Runs at most once per window. */
  private void sweep() {
    long now = now();
    if (now - lastSweep < windowSeconds * 1000L) {
      return;
    }
    lastSweep = now;
    long cutoff = now - windowSeconds * 1000L;
    buckets.entrySet().removeIf(e -> e.getValue().windowStart.get() < cutoff);
  }

  private static long now() {
    return System.currentTimeMillis();
  }
}
