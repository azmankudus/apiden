package com.example.apiden.module.hello;

import io.micronaut.serde.annotation.Serdeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A data record representing a simple hello message.
 * This record is annotated with @Serdeable to enable JSON serialization and deserialization.
 *
 * @param message The greeting message to be carried by this record.
 */
@Serdeable
final record Hello(String message) {

  /** logger instance for Hello record. */
  private static final Logger log = LoggerFactory.getLogger(Hello.class);

  /**
   * Main constructor for Hello record.
   *
   * @param message The greeting message.
   */
  Hello(final String message) {
    // Log the creation of a new Hello record at various levels for demonstration
    log.trace("Initializing Hello record with message: {}", message);
    log.debug("New Hello instance created.");

    this.message = message;

    // Additional logging if message is empty or null
    if (message == null || message.isBlank()) {
      log.warn("Hello record initialized with null or blank message.");
    }
  }

  /**
   * Retrieves the message from the record.
   *
   * @return The greeting message.
   */
  @Override
  public String message() {
    // Log the retrieval of the message
    log.trace("Message retrieved: {}", message);
    return message;
  }
}
