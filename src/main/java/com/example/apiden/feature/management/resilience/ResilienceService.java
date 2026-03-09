package com.example.apiden.feature.management.resilience;

import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulated external service used by all resilience demo controllers.
 *
 * <p>This service provides configurable failure behaviour so that
 * resilience patterns (circuit-breaker, retry, bulkhead, etc.) can be
 * exercised and observed through their respective controller endpoints.</p>
 *
 * <p>Call counters and failure modes are deliberately mutable so the
 * demo endpoints can toggle them at runtime.</p>
 */
@Singleton
public final class ResilienceService {

  private static final Logger logger = LoggerFactory.getLogger(ResilienceService.class);

  /** Tracks the total number of invocations for observability. */
  private final AtomicInteger callCounter = new AtomicInteger(0);

  /** When {@code true}, {@link #call()} will always throw an exception. */
  private volatile boolean failMode = false;

  /** Artificial delay in milliseconds injected by {@link #slowCall()}. */
  private volatile long delayMillis = 3000L;

  /**
   * Constructs a new ResilienceService.
   */
  ResilienceService() {
  }

  /**
   * Simulates an external service call.
   *
   * <p>When {@code failMode} is enabled, this method throws a
   * {@link RuntimeException} to trigger resilience fallback paths.</p>
   *
   * @return a map containing the call count and a status message
   * @throws RuntimeException if fail mode is enabled
   */
  public Map<String, Object> call() {
    int count = callCounter.incrementAndGet();
    logger.info("ResilienceService.call() invoked — attempt #{}", count);

    if (failMode) {
      logger.warn("Fail mode is ON — simulating service failure at attempt #{}", count);
      throw new RuntimeException("Simulated service failure at attempt #" + count);
    }

    return Map.of(
        "status", "success",
        "attempt", count,
        "message", "Service responded normally");
  }

  /**
   * Simulates a slow external service call with configurable latency.
   *
   * <p>This method is primarily used by the TimeLimiter demo to trigger
   * timeout behaviour.</p>
   *
   * @return a completable future that resolves after the configured delay
   */
  public CompletableFuture<Map<String, Object>> slowCall() {
    int count = callCounter.incrementAndGet();
    logger.info("ResilienceService.slowCall() invoked — attempt #{}, delay={}ms", count, delayMillis);

    return CompletableFuture.supplyAsync(() -> {
      try {
        Thread.sleep(delayMillis);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.warn("slowCall() interrupted at attempt #{}", count);
      }

      if (failMode) {
        throw new RuntimeException("Simulated slow service failure at attempt #" + count);
      }

      return Map.of(
          "status", "success",
          "attempt", count,
          "delay_ms", delayMillis,
          "message", "Slow service responded after delay");
    });
  }

  /**
   * Enables or disables forced-failure mode.
   *
   * @param fail {@code true} to make all subsequent calls fail
   */
  public void setFailMode(final boolean fail) {
    logger.info("Fail mode changed to: {}", fail);
    this.failMode = fail;
  }

  /**
   * Returns the current fail-mode state.
   *
   * @return {@code true} if fail mode is active
   */
  public boolean isFailMode() {
    return failMode;
  }

  /**
   * Configures the artificial delay for {@link #slowCall()}.
   *
   * @param millis delay in milliseconds
   */
  public void setDelayMillis(final long millis) {
    logger.info("Delay changed to: {}ms", millis);
    this.delayMillis = millis;
  }

  /**
   * Returns the current call count.
   *
   * @return total number of invocations
   */
  public int getCallCount() {
    return callCounter.get();
  }

  /**
   * Resets the call counter to zero.
   */
  public void resetCounter() {
    callCounter.set(0);
    logger.info("Call counter reset to 0");
  }
}
