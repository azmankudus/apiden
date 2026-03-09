package com.example.apiden.feature.management.resilience;

import com.example.apiden.core.web.ApiBody;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Consumes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Demonstrates the <strong>Time Limiter</strong> resilience pattern.
 *
 * <p>A time limiter enforces a maximum duration for an asynchronous operation.
 * If the downstream call exceeds the configured timeout the future is cancelled
 * and the fallback is invoked. This prevents slow dependencies from blocking
 * callers indefinitely.</p>
 *
 * <h3>Best-practice guidance</h3>
 * <ul>
 *   <li>Set {@code timeoutDuration} based on your SLA requirements plus a reasonable
 *       buffer for network jitter.</li>
 *   <li>Enable {@code cancelRunningFuture} to free up threads when the timeout fires.</li>
 *   <li>Always provide a fallback to return a meaningful response rather than
 *       propagating a {@code TimeoutException}.</li>
 *   <li>Combine with a Bulkhead so that timed-out futures do not accumulate and
 *       exhaust the thread pool.</li>
 * </ul>
 */
@Tag(name = "Resilience", description = "Microservices resilience pattern demonstrations")
@Controller("/resilience/time-limiter")
public class TimeLimiterController {

  private static final Logger logger = LoggerFactory.getLogger(TimeLimiterController.class);
  private final ResilienceService resilienceService;

  /**
   * Constructs a new TimeLimiterController.
   *
   * @param resilienceService the shared resilience demo service
   */
  TimeLimiterController(final ResilienceService resilienceService) {
    this.resilienceService = resilienceService;
  }

  /**
   * Invokes the slow downstream service with a time limit.
   *
   * <p>If the simulated service does not respond within the configured
   * {@code timeoutDuration}, the {@link #fallback(Throwable)} is invoked.</p>
   *
   * @return a completable future wrapping the response or fallback
   */
  @Operation(
      summary = "Time Limiter demo",
      description = "Calls the simulated slow service behind a time limiter. "
          + "Adjust delay via PUT /resilience/time-limiter/delay to trigger timeouts.")
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  @TimeLimiter(name = "backendService", fallbackMethod = "fallback")
  public CompletableFuture<Map<String, Object>> invoke() {
    logger.info("TimeLimiterController — invoking slow downstream service");
    return resilienceService.slowCall()
        .thenApply(result -> Map.of("pattern", "time-limiter", "result", (Object) result));
  }

  /**
   * Configures the artificial delay of the simulated service.
   *
   * @param body delay in milliseconds (e.g. {@code "5000"} to exceed timeout)
   * @return the new delay value
   */
  @Operation(summary = "Set service delay", description = "Configures the delay (ms) for the simulated slow service.")
  @Put("/delay")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> setDelay(@ApiBody final String body) {
    long millis = Long.parseLong(body);
    resilienceService.setDelayMillis(millis);
    logger.info("Service delay set to {}ms", millis);
    return Map.of("delay_ms", millis);
  }

  /**
   * Fallback invoked when the time limit is exceeded.
   *
   * @param throwable the timeout exception
   * @return a degraded response indicating a timeout
   */
  public CompletableFuture<Map<String, Object>> fallback(final Throwable throwable) {
    logger.warn("TimeLimiter fallback triggered: {}", throwable.getMessage());
    return CompletableFuture.completedFuture(Map.of(
        "pattern", "time-limiter",
        "status", "timeout",
        "reason", throwable.getMessage(),
        "message", "Service did not respond within the configured time limit"));
  }
}
