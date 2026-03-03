package com.example.apiden;

import io.micronaut.runtime.Micronaut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The entry point for the Apiden application.
 * This class initializes the Micronaut framework and starts the server.
 */
public final class Main {

  /** logger instance for Main class. */
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  /**
   * Default constructor for Main class.
   * Package-private to prevent external instantiation while allowing access within the package.
   */
  Main() {
  }

  /**
   * Application main entry point.
   *
   * @param args Command line arguments passed to the application.
   * @throws Exception if application fails to start.
   */
  public static void main(final String[] args) throws Exception {

    // Log the start of the application initialization process
    log.info("Starting Apiden application...");

    try {
      // Build and start the Micronaut application
      Micronaut.build(args)
          .classes(Main.class)
          .banner(false)
          .start();

      // Log successful startup
      log.info("Apiden application started successfully.");
    } catch (final Exception e) {
      // Log critical failure during startup
      log.error("Failed to start Apiden application: {}", e.getMessage(), e);
      throw e;
    }

  }
}
