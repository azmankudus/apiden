package com.example.apiden.feature.management.resilience;

import com.example.apiden.core.web.ApiBody;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

/**
 * Demonstrates the <strong>Circuit Breaker</strong> resilience pattern.
 *
 * <p>A circuit breaker monitors downstream call success/failure ratios. When the
 * failure rate exceeds a threshold the circuit <em>opens</em>, immediately
 * rejecting calls without attempting them. After a wait period it transitions to
 * <em>half-open</em>, allowing a limited number of probes. If those succeed the
 * circuit <em>closes</em> again; otherwise it re-opens.</p>
 *
 * <h3>Best-practice guidance</h3>
 * <ul>
 *   <li>Use a <strong>count-based or time-based sliding window</strong> to detect failures.</li>
 *   <li>Always provide a <strong>fallback method</strong> so the caller gets a
 *       graceful degraded response rather than an exception.</li>
 *   <li>Keep {@code waitDurationInOpenState} short enough to detect recovery quickly
 *       but long enough to avoid hammering a struggling service.</li>
 * </ul>
 */
@Tag(name = "Resilience", description = "Microservices resilience pattern demonstrations")
@Controller("/resilience/circuit-breaker")
public class CircuitBreakerController {

  private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerController.class);
  private final ResilienceService resilienceService;

  /**
   * Constructs a new CircuitBreakerController.
   *
   * @param resilienceService the shared resilience demo service
   */
  CircuitBreakerController(final ResilienceService resilienceService) {
    this.resilienceService = resilienceService;
  }

  /**
   * Invokes the downstream service with circuit-breaker protection.
   *
   * <p>When the circuit is open, the {@link #fallback(Throwable)} method is
   * invoked instead of the real service.</p>
   *
   * @return success payload or fallback response
   */
  @Operation(
      summary = "Circuit Breaker demo",
      description = "Calls the simulated service behind a circuit breaker. "
          + "Toggle fail mode via PUT /resilience/circuit-breaker/fail-mode to trip the circuit.")
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  @CircuitBreaker(name = "backendService", fallbackMethod = "fallback")
  public Map<String, Object> invoke() {
    logger.info("CircuitBreakerController — invoking downstream service");
    return Map.of("pattern", "circuit-breaker", "result", resilienceService.call());
  }

  /**
   * Toggles the fail mode of the simulated service.
   *
   * @param body {@code "true"} or {@code "false"}
   * @return the new fail-mode state
   */
  @Operation(summary = "Toggle fail mode", description = "Enables/disables forced failures to trip the circuit breaker.")
  @Put("/fail-mode")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> toggleFailMode(@ApiBody final String body) {
    boolean fail = Boolean.parseBoolean(body);
    resilienceService.setFailMode(fail);
    logger.info("Fail mode set to: {}", fail);
    return Map.of("fail_mode", fail);
  }

  /**
   * Fallback invoked when the circuit is open or the call fails.
   *
   * @param throwable the cause of the failure
   * @return a degraded but non-exceptional response
   */
  public Map<String, Object> fallback(final Throwable throwable) {
    logger.warn("CircuitBreaker fallback triggered: {}", throwable.getMessage());
    return Map.of(
        "pattern", "circuit-breaker",
        "status", "fallback",
        "reason", throwable.getMessage(),
        "message", "Circuit breaker is open — returning degraded response");
  }
}
