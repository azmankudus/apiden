package com.example.apiden.feature.management.resilience;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralised, type-safe configuration for all Resilience4j pattern instances.
 *
 * <p>Properties are bound from {@code resilience} prefix in
 * {@code application.properties}. Each nested record maps to one Resilience4j
 * concept so configuration is discoverable, documented, and refactoring-safe.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * resilience.enabled=true
 * resilience.metrics-enabled=true
 * }</pre>
 */
@ConfigurationProperties("resilience")
public final class ResilienceConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(ResilienceConfiguration.class);

  /** Global toggle to enable or disable all resilience patterns at once. */
  private boolean enabled = true;

  /** Whether Resilience4j metrics should be exported to Micrometer. */
  private boolean metricsEnabled = true;

  /** Optional description surfaced in management/health endpoints. */
  @Nullable
  private String description;

  /**
   * Constructs a new ResilienceConfiguration with default values.
   */
  ResilienceConfiguration() {
    logger.info("ResilienceConfiguration initialised — enabled={}, metricsEnabled={}", enabled, metricsEnabled);
  }

  /**
   * Returns whether resilience patterns are globally enabled.
   *
   * @return {@code true} if resilience is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Enables or disables all resilience patterns globally.
   *
   * @param enabled {@code true} to enable, {@code false} to disable
   */
  public void setEnabled(final boolean enabled) {
    logger.info("Resilience enabled changed: {} → {}", this.enabled, enabled);
    this.enabled = enabled;
  }

  /**
   * Returns whether Resilience4j metrics export is enabled.
   *
   * @return {@code true} if metrics are exported
   */
  public boolean isMetricsEnabled() {
    return metricsEnabled;
  }

  /**
   * Enables or disables Resilience4j metrics export.
   *
   * @param metricsEnabled {@code true} to enable metrics export
   */
  public void setMetricsEnabled(final boolean metricsEnabled) {
    logger.info("Resilience metrics enabled changed: {} → {}", this.metricsEnabled, metricsEnabled);
    this.metricsEnabled = metricsEnabled;
  }

  /**
   * Returns the optional description.
   *
   * @return the description, or {@code null} if not set
   */
  @Nullable
  public String getDescription() {
    return description;
  }

  /**
   * Sets the optional description.
   *
   * @param description the description text
   */
  public void setDescription(@Nullable final String description) {
    this.description = description;
  }

  /**
   * Summary view of Resilience4j configuration used by the info endpoint.
   *
   * @return a human-readable summary of the current resilience configuration
   */
  @Serdeable
  public record Summary(
      boolean enabled,
      boolean metricsEnabled,
      @Nullable String description
  ) {
  }

  /**
   * Builds a serialisable summary of the current configuration.
   *
   * @return a snapshot summary
   */
  public Summary toSummary() {
    return new Summary(enabled, metricsEnabled, description);
  }
}
