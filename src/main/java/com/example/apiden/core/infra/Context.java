package com.example.apiden.core.infra;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.MDC;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.propagation.ThreadPropagatedContextElement;

/**
 * Manages request-scoped data using {@link ThreadLocal} with propagation support.
 * 
 * <p>This record implements {@link ThreadPropagatedContextElement} to ensure the context
 * data and SLF4J {@link MDC} are correctly preserved across asynchronous boundaries
 * (e.g., when switching threads in Netty or scheduled tasks).</p>
 * 
 * @param data the map containing the context attributes
 */
public record Context(Map<String, Object> data) implements ThreadPropagatedContextElement<Map<String, Object>> {

  private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

  /**
   * Initializes the context for the current thread.
   *
   * @param initialData initial attributes to populate
   */
  public static void init(Map<String, Object> initialData) {
    THREAD_LOCAL.set(new ConcurrentHashMap<>(initialData));
  }

  /**
   * Cleans up the context for the current thread and clears MDC.
   */
  public static void destroy() {
    THREAD_LOCAL.remove();
    MDC.clear();
  }

  /**
   * @return the raw data map for the current context
   */
  public static Map<String, Object> getMap() {
    return THREAD_LOCAL.get();
  }

  /**
   * Retrieves an attribute from the context.
   *
   * @param <T> the type of the value
   * @param name the attribute name
   * @param defaultValue the value to return if not found
   * @return the attribute value or default
   */
  @SuppressWarnings("unchecked")
  public static <T> T get(final String name, final T defaultValue) {
    Map<String, Object> map = THREAD_LOCAL.get();
    if (map != null && map.containsKey(name)) {
      return (T) map.get(name);
    }
    return defaultValue;
  }

  /**
   * Sets or updates an attribute in the current context.
   *
   * @param name attribute name
   * @param value attribute value
   * @throws IllegalStateException if context is not initialized
   */
  public static void set(final String name, final Object value) {
    Map<String, Object> map = THREAD_LOCAL.get();
    if (map == null) {
      throw new IllegalStateException("Context not initialized");
    }
    map.put(name, value);
  }

  /**
   * Updates the thread-local state and MDC with the data from this context element.
   *
   * @return The previous state of the context map.
   */
  @Override
  public @Nullable Map<String, Object> updateThreadContext() {
    Map<String, Object> oldState = THREAD_LOCAL.get();
    THREAD_LOCAL.set(data);
    data.forEach((k, v) -> {
      if (v != null) {
        MDC.put(k, v.toString());
      }
    });
    return oldState;
  }

  /**
   * Restores the thread-local state and MDC to the previous state.
   *
   * @param oldState The previous state of the context map.
   */
  @Override
  public void restoreThreadContext(@Nullable Map<String, Object> oldState) {
    if (oldState == null) {
      THREAD_LOCAL.remove();
      MDC.clear();
    } else {
      THREAD_LOCAL.set(oldState);
      MDC.clear();
      oldState.forEach((k, v) -> {
        if (v != null) {
          MDC.put(k, v.toString());
        }
      });
    }
  }
}
