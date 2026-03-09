package com.example.apiden.feature.management.resilience;

import com.example.apiden.core.web.ApiBody;
import io.github.resilience4j.retry.annotation.Retry;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.context.annotation.Executable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Demonstrates the <strong>Retry</strong> resilience pattern.
 *
 * <p>Retry automatically re-invokes a failed operation a configurable number of
 * times with optional exponential back-off. It is ideal for <em>transient</em>
 * failures such as brief network blips or temporary service unavailability.</p>
 *
 * <h3>Best-practice guidance</h3>
 * <ul>
 *   <li>Only retry on <strong>transient, recoverable</strong> errors — avoid retrying
 *       on validation failures or 4xx responses.</li>
 *   <li>Use <strong>exponential back-off with jitter</strong> to avoid thundering-herd
 *       effects.</li>
 *   <li>Set a reasonable {@code maxAttempts} (typically 3–5) to avoid excessive latency.</li>
 *   <li>Combine with a Circuit Breaker so repeated retries do not mask a persistent
 *       outage.</li>
 * </ul>
 */
@Tag(name = "Resilience", description = "Microservices resilience pattern demonstrations")
@Controller("/resilience/retry")
public class RetryController {

  private static final Logger logger = LoggerFactory.getLogger(RetryController.class);
  private final ResilienceService resilienceService;

  /**
   * Constructs a new RetryController.
   *
   * @param resilienceService the shared resilience demo service
   */
  RetryController(final ResilienceService resilienceService) {
    this.resilienceService = resilienceService;
  }

  /**
   * Invokes the downstream service with automatic retry on failure.
   *
   * <p>If the call fails, Resilience4j retries up to the configured
   * {@code maxAttempts} with exponential back-off before invoking the
   * {@link #fallback(Throwable)} method.</p>
   *
   * @return success payload or fallback response
   */
  @Operation(
      summary = "Retry demo",
      description = "Calls the simulated service with automatic retry. "
          + "Toggle fail mode via PUT /resilience/retry/fail-mode to trigger retries.")
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  @Retry(name = "backendService", fallbackMethod = "fallback")
  @Executable
  public Map<String, Object> invoke() {
    logger.info("RetryController — invoking downstream service (attempt will be logged by service)");
    return Map.of("pattern", "retry", "result", resilienceService.call());
  }

  /**
   * Toggles the fail mode of the simulated service.
   *
   * @param body {@code "true"} or {@code "false"}
   * @return the new fail-mode state
   */
  @Operation(summary = "Toggle fail mode", description = "Enables/disables forced failures to trigger retries.")
  @Put("/fail-mode")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> toggleFailMode(@ApiBody final String body) {
    boolean fail = Boolean.parseBoolean(body);
    resilienceService.setFailMode(fail);
    return Map.of("fail_mode", fail);
  }

  /**
   * Resets the call counter so retry attempts are easier to observe.
   *
   * @return confirmation with reset counter value
   */
  @Operation(summary = "Reset counter", description = "Resets the call counter to zero for observability.")
  @Put("/reset")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> reset() {
    resilienceService.resetCounter();
    return Map.of("counter", 0, "message", "Counter reset");
  }

  /**
   * Fallback invoked after all retry attempts are exhausted.
   *
   * @param throwable the cause of the last failure
   * @return a degraded but non-exceptional response
   */
  public Map<String, Object> fallback(final Throwable throwable) {
    logger.warn("Retry fallback triggered after all attempts exhausted: {}", throwable.getMessage());
    return Map.of(
        "pattern", "retry",
        "status", "fallback",
        "reason", throwable.getMessage(),
        "total_attempts", resilienceService.getCallCount(),
        "message", "All retry attempts exhausted — returning degraded response");
  }
}
