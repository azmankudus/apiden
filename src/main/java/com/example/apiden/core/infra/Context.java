package com.example.apiden.core.infra;

import io.micronaut.core.propagation.MutablePropagatedContext;
import io.micronaut.core.propagation.PropagatedContext;
import io.micronaut.core.propagation.ThreadPropagatedContextElement;
import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for managing request-scoped metadata.
 * Provides a standalone API for initialization, data storage, and cleanup.
 */
public final class Context {

  private Context() {
  }

  private static final ThreadLocal<Map<String, Object>> FALLBACK_STORAGE = new ThreadLocal<>();

  /**
   * Initializes an empty context for the current thread/test scope.
   */
  public static void init() {
    FALLBACK_STORAGE.set(new ConcurrentHashMap<>());
  }

  /**
   * Initializes the context within a Micronaut propagation chain.
   *
   * @param propagatedContext the mutable context from the filter
   */
  public static void init(final MutablePropagatedContext propagatedContext) {
    Map<String, Object> map = new ConcurrentHashMap<>();
    propagatedContext.add(new ContextElement(map));
    FALLBACK_STORAGE.set(map);
  }

  /**
   * Stores an attribute in the current context.
   *
   * @param name  attribute name
   * @param value attribute value
   */
  public static void set(final String name, final Object value) {
    Map<String, Object> map = getMap();
    if (map == null) {
      throw new IllegalStateException("Context not initialized");
    }
    map.put(name, value);
    if (value != null) {
      MDC.put(name, value.toString());
    } else {
      MDC.remove(name);
    }
  }

  /**
   * Retrieves an attribute from the current context.
   *
   * @param <T>  expected type
   * @param name attribute name
   * @return the value or null if not found/initialized
   */
  @SuppressWarnings("unchecked")
  public static <T> T get(final String name) {
    Map<String, Object> map = getMap();
    return map != null ? (T) map.get(name) : null;
  }

  /**
   * Retrieves an attribute with a default value fallback.
   *
   * @param <T>          expected type
   * @param name         attribute name
   * @param defaultValue default value if not found
   * @return the value or defaultValue
   */
  public static <T> T get(final String name, final T defaultValue) {
    T value = get(name);
    return value != null ? value : defaultValue;
  }

  /**
   * Cleans up the context and MDC for the current thread.
   */
  public static void destroy() {
    FALLBACK_STORAGE.remove();
    MDC.clear();
  }

  /**
   * @return the active data map, either from propagation or local fallback.
   */
  private static Map<String, Object> getMap() {
    try {
      return PropagatedContext.get()
          .find(ContextElement.class)
          .map(ContextElement::data)
          .orElseGet(FALLBACK_STORAGE::get);
    } catch (Exception e) {
      return FALLBACK_STORAGE.get();
    }
  }

  /**
   * Internal element for Micronaut thread propagation.
   */
  private record ContextElement(Map<String, Object> data)
      implements ThreadPropagatedContextElement<Map<String, String>> {

    @Override
    public Map<String, String> updateThreadContext() {
      Map<String, String> oldState = MDC.getCopyOfContextMap();
      if (data != null) {
        data.forEach((k, v) -> {
          if (v != null) {
            MDC.put(k, v.toString());
          }
        });
      }
      return oldState;
    }

    @Override
    public void restoreThreadContext(Map<String, String> oldState) {
      MDC.clear();
      if (oldState != null) {
        MDC.setContextMap(oldState);
      }
    }
  }
}
