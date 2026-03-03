package com.example.apiden.shared.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom exception class for representing API-level errors.
 * Includes a specific error code along with the exception message.
 */
public final class ApiException extends Exception {

  /** logger instance for ApiException. */
  private static final Logger log = LoggerFactory.getLogger(ApiException.class);

  private final String code;

  /**
   * Constructs a new ApiException with the specified error code and detail message.
   *
   * @param code The API error code.
   * @param message The detail message.
   */
  public ApiException(final String code, final String message) {
    super(message);
    this.code = code;
    log.debug("Creating ApiException: code={}, message={}", code, message);
    log.trace("ApiException created without cause.");
  }

  /**
   * Constructs a new ApiException with the specified error code, detail message, and cause.
   *
   * @param code The API error code.
   * @param message The detail message.
   * @param cause The cause of the exception.
   */
  public ApiException(final String code, final String message, final Throwable cause) {
    super(message, cause);
    this.code = code;
    log.debug("Creating ApiException with cause: code={}, message={}", code, message);
    if (cause != null) {
      log.trace("ApiException cause: {}", cause.getMessage());
    }
  }

  /**
   * Returns the error code associated with this exception.
   *
   * @return The error code.
   */
  public String getCode() {
    log.trace("Retrieving error code: {}", code);
    return code;
  }
}
