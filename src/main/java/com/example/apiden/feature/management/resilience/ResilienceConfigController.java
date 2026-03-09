package com.example.apiden.feature.management.resilience;

import com.example.apiden.core.web.ApiBody;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Produces;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Management endpoint that exposes the current resilience configuration and
 * allows runtime toggling of global resilience and metrics flags.
 *
 * <p>This controller aggregates all resilience-related settings into a single
 * view so operators can inspect and adjust them without restarting the
 * application.</p>
 *
 * <h3>Endpoints</h3>
 * <ul>
 *   <li>{@code GET  /resilience/config} — returns current configuration</li>
 *   <li>{@code PUT  /resilience/config/enabled} — toggles global resilience</li>
 *   <li>{@code PUT  /resilience/config/metrics} — toggles metrics export</li>
 * </ul>
 */
@Tag(name = "Resilience", description = "Microservices resilience pattern demonstrations")
@Controller("/resilience/config")
public final class ResilienceConfigController {

  private static final Logger logger = LoggerFactory.getLogger(ResilienceConfigController.class);
  private final ResilienceConfiguration resilienceConfiguration;

  /**
   * Constructs a new ResilienceConfigController.
   *
   * @param resilienceConfiguration the injected resilience configuration
   */
  ResilienceConfigController(final ResilienceConfiguration resilienceConfiguration) {
    this.resilienceConfiguration = resilienceConfiguration;
  }

  /**
   * Returns the current resilience configuration summary.
   *
   * <p>Includes the global enabled flag, metrics-export flag, and a
   * description field.</p>
   *
   * @return a map containing the current configuration
   */
  @Operation(
      summary = "Get resilience configuration",
      description = "Returns the current resilience configuration including "
          + "global enabled/disabled state and metrics export settings.")
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getConfig() {
    logger.info("Returning current resilience configuration");
    ResilienceConfiguration.Summary summary = resilienceConfiguration.toSummary();

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("enabled", summary.enabled());
    result.put("metricsEnabled", summary.metricsEnabled());
    result.put("description", summary.description());
    result.put("patterns", Map.of(
        "circuitBreaker", "resilience4j.circuitbreaker.instances.*",
        "retry", "resilience4j.retry.instances.*",
        "rateLimiter", "resilience4j.ratelimiter.instances.*",
        "timeLimiter", "resilience4j.timelimiter.instances.*",
        "bulkhead", "resilience4j.bulkhead.instances.*"
    ));
    return result;
  }

  /**
   * Toggles the global resilience enabled flag.
   *
   * <p>When disabled, resilience annotations are still processed but
   * the controllers can check {@code ResilienceConfiguration.isEnabled()}
   * to short-circuit pattern application.</p>
   *
   * @param body {@code "true"} or {@code "false"}
   * @return the updated enabled state
   */
  @Operation(
      summary = "Toggle global resilience",
      description = "Enables or disables all resilience patterns globally.")
  @Put("/enabled")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> toggleEnabled(@ApiBody final String body) {
    boolean enabled = Boolean.parseBoolean(body);
    resilienceConfiguration.setEnabled(enabled);
    logger.info("Global resilience enabled set to: {}", enabled);
    return Map.of("enabled", enabled, "message", enabled
        ? "Resilience patterns are now enabled"
        : "Resilience patterns are now disabled");
  }

  /**
   * Toggles Resilience4j metrics export to Micrometer.
   *
   * @param body {@code "true"} or {@code "false"}
   * @return the updated metrics state
   */
  @Operation(
      summary = "Toggle resilience metrics",
      description = "Enables or disables Resilience4j metrics export to Micrometer.")
  @Put("/metrics")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> toggleMetrics(@ApiBody final String body) {
    boolean metricsEnabled = Boolean.parseBoolean(body);
    resilienceConfiguration.setMetricsEnabled(metricsEnabled);
    logger.info("Resilience metrics enabled set to: {}", metricsEnabled);
    return Map.of("metricsEnabled", metricsEnabled, "message", metricsEnabled
        ? "Resilience metrics export is now enabled"
        : "Resilience metrics export is now disabled");
  }
}
