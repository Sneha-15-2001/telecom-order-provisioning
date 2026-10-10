package com.telecom.inventory.exception;

/**
 * The request was well-formed but conflicts with the current state of the
 * resource — a lifecycle violation such as submitting an order that is not
 * validated, or sending a notification that has already gone out.
 *
 * <p>Deliberately distinct from IllegalArgumentException. A malformed request
 * is a client mistake and returns 400. A valid request against the wrong state
 * is a conflict and returns 409: the caller can fix a 400 by changing the
 * payload, but a 409 by re-reading the resource and deciding what to do next.
 * Reporting every state violation as 400 hides that distinction from clients.
 */
public class StateConflictException extends RuntimeException {

  private final String code;

  public StateConflictException(String code, String message) {
    super(message);
    this.code = code;
  }

  /** Machine-readable reason, for example ORDER_STATE_INVALID. */
  public String getCode() {
    return code;
  }
}
