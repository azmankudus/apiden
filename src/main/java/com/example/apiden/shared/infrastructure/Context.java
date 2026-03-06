package com.example.apiden.shared.infrastructure;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.MDC;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.propagation.ThreadPropagatedContextElement;

public record Context(Map<String, Object> data) implements ThreadPropagatedContextElement<Map<String, Object>> {

  private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

  public static void init(Map<String, Object> initialData) {
    THREAD_LOCAL.set(new ConcurrentHashMap<>(initialData));
  }

  public static void destroy() {
    THREAD_LOCAL.remove();
    MDC.clear();
  }

  public static Map<String, Object> getMap() {
    return THREAD_LOCAL.get();
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(final String name, final T defaultValue) {
    Map<String, Object> map = THREAD_LOCAL.get();
    if (map != null && map.containsKey(name)) {
      return (T) map.get(name);
    }
    return defaultValue;
  }

  public static void set(final String name, final Object value) {
    Map<String, Object> map = THREAD_LOCAL.get();
    if (map == null) {
      throw new IllegalStateException("Context not initialized");
    }
    map.put(name, value);
  }

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
