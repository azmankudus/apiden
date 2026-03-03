package com.example.apiden.shared.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration of possible high-level API response statuses.
 */
public enum ResponseStatus {
  /** Indicates a successful operation. */
  SUCCESS("success"),
  /** Indicates an operation that completed with warnings. */
  WARNING("warning"),
  /** Indicates an operation that failed. */
  ERROR("error");

  private static final Logger log = LoggerFactory.getLogger(ResponseStatus.class);

  private final String value;

  /**
   * Private constructor for enum values.
   *
   * @param value The string representation of the status.
   */
  private ResponseStatus(final String value) {
    this.value = value;
  }

  /**
   * Retrieves the string representation of the status.
   *
   * @return The status value.
   */
  public String getValue() {
    log.trace("Retrieving value for ResponseStatus: {}", value);
    return value;
  }
}
