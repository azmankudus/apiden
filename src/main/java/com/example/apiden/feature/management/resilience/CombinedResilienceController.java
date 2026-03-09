package com.example.apiden.feature.management.resilience;

import com.example.apiden.core.web.ApiBody;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
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
 * Demonstrates <strong>combined/stacked</strong> resilience patterns on a single endpoint.
 *
 * <p>In production microservices you rarely use just one pattern in isolation.
 * This controller stacks <em>all five</em> Resilience4j patterns on a single
 * method to show the recommended ordering and how they compose.</p>
 *
 * <h3>Aspect ordering (outermost → innermost)</h3>
 * <ol>
 *   <li><strong>Retry</strong> — outermost; retries the entire decorated call.</li>
 *   <li><strong>CircuitBreaker</strong> — tracks retry-amplified failures.</li>
 *   <li><strong>RateLimiter</strong> — throttles the effective call rate.</li>
 *   <li><strong>TimeLimiter</strong> — aborts slow calls.</li>
 *   <li><strong>Bulkhead</strong> — innermost; limits concurrency.</li>
 * </ol>
 *
 * <h3>Best-practice guidance</h3>
 * <ul>
 *   <li>Use a <strong>shared configuration instance name</strong> (e.g.
 *       {@code "combinedService"}) so all patterns reference the same logical
 *       instance and metrics are correlated.</li>
 *   <li>Be mindful that Retry × CircuitBreaker can generate many calls —
 *       make sure the RateLimiter covers the amplified rate.</li>
 *   <li>Always define a <strong>single fallback</strong> at the outermost level
 *       to handle any exception from any layer.</li>
 * </ul>
 */
@Tag(name = "Resilience", description = "Microservices resilience pattern demonstrations")
@Controller("/resilience/combined")
public class CombinedResilienceController {

  private static final Logger logger = LoggerFactory.getLogger(CombinedResilienceController.class);
  private final ResilienceService resilienceService;

  /**
   * Constructs a new CombinedResilienceController.
   *
   * @param resilienceService the shared resilience demo service
   */
  CombinedResilienceController(final ResilienceService resilienceService) {
    this.resilienceService = resilienceService;
  }

  /**
   * Invokes the downstream service with all five resilience patterns stacked.
   *
   * <p>The annotations are evaluated in Resilience4j's standard order:
   * Retry → CircuitBreaker → RateLimiter → TimeLimiter → Bulkhead.</p>
   *
   * @return a completable future wrapping the success payload or fallback
   */
  @Operation(
      summary = "Combined resilience demo",
      description = "Calls the simulated service with all five resilience patterns stacked: "
          + "Retry, CircuitBreaker, RateLimiter, TimeLimiter, and Bulkhead.")
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  @Retry(name = "combinedService")
  @CircuitBreaker(name = "combinedService")
  @RateLimiter(name = "combinedService")
  @TimeLimiter(name = "combinedService", fallbackMethod = "fallback")
  @Bulkhead(name = "combinedService")
  public CompletableFuture<Map<String, Object>> invoke() {
    logger.info("CombinedResilienceController — invoking service with all patterns stacked");
    return resilienceService.slowCall()
        .thenApply(result -> Map.of(
            "pattern", "combined",
            "patterns_applied", "retry → circuit-breaker → rate-limiter → time-limiter → bulkhead",
            "result", (Object) result));
  }

  /**
   * Toggles the fail mode of the simulated service.
   *
   * @param body {@code "true"} or {@code "false"}
   * @return the new fail-mode state
   */
  @Operation(summary = "Toggle fail mode", description = "Enables/disables forced failures for the combined demo.")
  @Put("/fail-mode")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> toggleFailMode(@ApiBody final String body) {
    boolean fail = Boolean.parseBoolean(body);
    resilienceService.setFailMode(fail);
    return Map.of("fail_mode", fail);
  }

  /**
   * Configures the artificial delay of the simulated service.
   *
   * @param body delay in milliseconds
   * @return the new delay value
   */
  @Operation(summary = "Set service delay", description = "Configures the delay (ms) for the combined demo.")
  @Put("/delay")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> setDelay(@ApiBody final String body) {
    long millis = Long.parseLong(body);
    resilienceService.setDelayMillis(millis);
    return Map.of("delay_ms", millis);
  }

  /**
   * Combined fallback for any resilience failure across all layers.
   *
   * @param throwable the exception from whichever layer triggered first
   * @return a degraded but informative response
   */
  public CompletableFuture<Map<String, Object>> fallback(final Throwable throwable) {
    logger.warn("Combined resilience fallback triggered: {}", throwable.getMessage());
    return CompletableFuture.completedFuture(Map.of(
        "pattern", "combined",
        "status", "fallback",
        "triggered_by", throwable.getClass().getSimpleName(),
        "reason", throwable.getMessage(),
        "message", "One of the stacked resilience patterns triggered a fallback"));
  }
}
