package com.example.apiden.feature.management.resilience;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

/**
 * Demonstrates the <strong>Rate Limiter</strong> resilience pattern.
 *
 * <p>Rate limiting controls how many calls are permitted within a time window.
 * Excess calls are either queued or immediately rejected, protecting downstream
 * services from being overwhelmed.</p>
 *
 * <h3>Best-practice guidance</h3>
 * <ul>
 *   <li>Set {@code limitForPeriod} based on the downstream service's
 *       documented throughput capacity.</li>
 *   <li>Use {@code limitRefreshPeriod} to align with realistic traffic patterns
 *       (e.g., 1 second for API calls, 1 minute for batch operations).</li>
 *   <li>Provide a {@code timeoutDuration} of 0 for immediate rejection, or a
 *       small value to allow brief queuing.</li>
 *   <li>Always return a clear <strong>429 Too Many Requests</strong>-style
 *       message in the fallback so clients know to back off.</li>
 * </ul>
 */
@Tag(name = "Resilience", description = "Microservices resilience pattern demonstrations")
@Controller("/resilience/rate-limiter")
public class RateLimiterController {

  private static final Logger logger = LoggerFactory.getLogger(RateLimiterController.class);
  private final ResilienceService resilienceService;

  /**
   * Constructs a new RateLimiterController.
   *
   * @param resilienceService the shared resilience demo service
   */
  RateLimiterController(final ResilienceService resilienceService) {
    this.resilienceService = resilienceService;
  }

  /**
   * Invokes the downstream service with rate limiting.
   *
   * <p>If the call rate exceeds the configured limit, the
   * {@link #fallback(Throwable)} method is invoked with a
   * {@code RequestNotPermitted} exception.</p>
   *
   * @return success payload or fallback response
   */
  @Operation(
      summary = "Rate Limiter demo",
      description = "Calls the simulated service behind a rate limiter. "
          + "Rapid consecutive requests will be throttled and receive a fallback response.")
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  @RateLimiter(name = "backendService", fallbackMethod = "fallback")
  public Map<String, Object> invoke() {
    logger.info("RateLimiterController — request permitted at {}", Instant.now());
    return Map.of(
        "pattern", "rate-limiter",
        "result", resilienceService.call(),
        "timestamp", Instant.now().toString());
  }

  /**
   * Fallback invoked when the rate limit is exceeded.
   *
   * @param throwable the rate-limit exception
   * @return a throttled response indicating the client should back off
   */
  public Map<String, Object> fallback(final Throwable throwable) {
    logger.warn("RateLimiter fallback triggered: {}", throwable.getMessage());
    return Map.of(
        "pattern", "rate-limiter",
        "status", "throttled",
        "reason", throwable.getMessage(),
        "timestamp", Instant.now().toString(),
        "message", "Rate limit exceeded — please retry after a short delay");
  }
}
