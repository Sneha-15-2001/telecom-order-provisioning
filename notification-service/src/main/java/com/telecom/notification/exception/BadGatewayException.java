package com.telecom.notification.exception;

/**
 * An upstream service answered, but with something this service cannot use — a
 * 5xx, an unreadable body, or a response missing fields it must have.
 *
 * <p>Mapped to 502 Bad Gateway: this service is the one the client called, and
 * the fault lies with the service behind it. Distinct from
 * {@link DependencyUnavailableException}, which means the upstream did not
 * answer at all.
 */
public class BadGatewayException extends RuntimeException {

  private final String dependency;
  private final int upstreamStatus;

  public BadGatewayException(String dependency, int upstreamStatus, String message) {
    super(message);
    this.dependency = dependency;
    this.upstreamStatus = upstreamStatus;
  }

  public BadGatewayException(String dependency, int upstreamStatus, String message, Throwable cause) {
    super(message, cause);
    this.dependency = dependency;
    this.upstreamStatus = upstreamStatus;
  }

  public String getDependency() {
    return dependency;
  }

  /** The status the upstream returned, so the fault is attributable. */
  public int getUpstreamStatus() {
    return upstreamStatus;
  }
}
