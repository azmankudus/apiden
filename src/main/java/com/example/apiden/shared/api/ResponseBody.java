package com.example.apiden.shared.api;

import io.micronaut.core.util.StringUtils;
import io.micronaut.serde.annotation.Serdeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the standard response body structure for the API.
 * This record ensures that status, code, and message are always populated with sensible defaults.
 *
 * @param status The high-level status of the response (e.g., SUCCESS, ERROR).
 * @param code A specific application error or success code.
 * @param message A human-readable message describing the outcome.
 * @param body The actual payload of the response.
 */
@Serdeable
public final record ResponseBody(
    ResponseStatus status,
    String code,
    String message,
    Object body) {

  private static final Logger log = LoggerFactory.getLogger(ResponseBody.class);

  /**
   * Compact constructor for ResponseBody.
   * Performs validation and sets default values if necessary.
   */
  public ResponseBody {
    log.trace("Initializing ResponseBody with status: {}, code: {}", status, code);

    // Apply defaults if fields are missing
    if (!StringUtils.hasText(code)) {
      log.debug("No code provided; defaulting to '0'.");
      code = "0";
    }
    if (!StringUtils.hasText(message)) {
      log.debug("No message provided; defaulting to 'Success'.");
      message = "Success";
    }
    if (status == null) {
      log.warn("Null status provided; defaulting to SUCCESS.");
      status = ResponseStatus.SUCCESS;
    }

    log.debug("ResponseBody successfully initialized: status={}, code={}", status, code);
  }
}
