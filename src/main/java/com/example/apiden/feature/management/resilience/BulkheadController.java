package com.example.apiden.feature.management.resilience;

import com.example.apiden.core.web.ApiBody;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
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
 * Demonstrates the <strong>Bulkhead</strong> resilience pattern.
 *
 * <p>A bulkhead limits the number of concurrent calls to a downstream service,
 * preventing a slow or failing dependency from consuming all available threads
 * and starving other parts of the application. The name comes from ship
 * compartment design — if one compartment floods, the others remain dry.</p>
 *
 * <h3>Best-practice guidance</h3>
 * <ul>
 *   <li>Use <strong>semaphore-based</strong> bulkheads for lightweight in-process
 *       isolation (default). Use thread-pool-based bulkheads when you need true
 *       thread isolation (but be aware of the overhead).</li>
 *   <li>Set {@code maxConcurrentCalls} based on the downstream service's capacity
 *       and expected response times.</li>
 *   <li>Configure {@code maxWaitDuration} to control how long excess calls wait
 *       before being rejected (0 = immediate rejection).</li>
 *   <li>Combine with a Circuit Breaker so sustained concurrency pressure triggers
 *       circuit opening.</li>
 * </ul>
 */
@Tag(name = "Resilience", description = "Microservices resilience pattern demonstrations")
@Controller("/resilience/bulkhead")
public class BulkheadController {

  private static final Logger logger = LoggerFactory.getLogger(BulkheadController.class);
  private final ResilienceService resilienceService;

  /**
   * Constructs a new BulkheadController.
   *
   * @param resilienceService the shared resilience demo service
   */
  BulkheadController(final ResilienceService resilienceService) {
    this.resilienceService = resilienceService;
  }

  /**
   * Invokes the downstream service with bulkhead concurrency limiting.
   *
   * <p>If the maximum number of concurrent calls is reached, the
   * {@link #fallback(Throwable)} method is invoked with a
   * {@code BulkheadFullException}.</p>
   *
   * @return success payload or fallback response
   */
  @Operation(
      summary = "Bulkhead demo",
      description = "Calls the simulated service behind a bulkhead. "
          + "When max concurrent calls are reached, additional requests receive a fallback.")
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  @Bulkhead(name = "backendService", fallbackMethod = "fallback")
  public Map<String, Object> invoke() {
    logger.info("BulkheadController — acquired bulkhead permit, invoking service");

    // Introduce a small delay so concurrent requests can saturate the bulkhead
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    return Map.of("pattern", "bulkhead", "result", resilienceService.call());
  }

  /**
   * Toggles the delay injected into the downstream call to make
   * bulkhead saturation easier to observe.
   *
   * @param body delay in milliseconds (e.g. {@code "5000"})
   * @return the new delay value
   */
  @Operation(summary = "Set service delay", description = "Configures the delay for the simulated service to control bulkhead saturation.")
  @Put("/delay")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> setDelay(@ApiBody final String body) {
    long millis = Long.parseLong(body);
    resilienceService.setDelayMillis(millis);
    return Map.of("delay_ms", millis);
  }

  /**
   * Fallback invoked when the bulkhead is full.
   *
   * @param throwable the bulkhead-full exception
   * @return a degraded response indicating resource limits
   */
  public Map<String, Object> fallback(final Throwable throwable) {
    logger.warn("Bulkhead fallback triggered: {}", throwable.getMessage());
    return Map.of(
        "pattern", "bulkhead",
        "status", "rejected",
        "reason", throwable.getMessage(),
        "message", "Bulkhead is full — too many concurrent requests, please retry later");
  }
}
