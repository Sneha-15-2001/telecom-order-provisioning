package com.telecom.order.exception;

/**
 * A required dependency is down, timing out, or refusing work.
 *
 * <p>Mapped to 503 Service Unavailable, and deliberately distinct from a business
 * rejection. If customer-service is unreachable, the customer has not been
 * refused — nobody has answered yet. Returning 4xx would tell the caller to fix
 * a request that was perfectly valid, and marking the order FAILED would destroy
 * a customer order because of someone else's outage.
 *
 * <p>The correct behaviour is to leave the order in its previous, retryable
 * state and tell the caller to try again.
 */
public class DependencyUnavailableException extends RuntimeException {

  private final String dependency;

  public DependencyUnavailableException(String dependency, String message) {
    super(message);
    this.dependency = dependency;
  }

  public DependencyUnavailableException(String dependency, String message, Throwable cause) {
    super(message, cause);
    this.dependency = dependency;
  }

  /** Which downstream service is at fault, for the caller and the log. */
  public String getDependency() {
    return dependency;
  }
}
