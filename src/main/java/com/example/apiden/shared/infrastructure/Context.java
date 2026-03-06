package com.example.apiden.shared.infrastructure;

import java.util.Map;

import org.slf4j.MDC;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.propagation.PropagatedContext;
import io.micronaut.core.propagation.ThreadPropagatedContextElement;

public record Context(Map<String, Object> data) implements ThreadPropagatedContextElement<Map<String, String>> {

  @Override
  public @Nullable Map<String, String> updateThreadContext() {
    Map<String, String> oldState = MDC.getCopyOfContextMap();
    data.forEach((k, v) -> MDC.put(k, v.toString()));
    return oldState;
  }

  @Override
  public void restoreThreadContext(@Nullable Map<String, String> oldState) {
    if (oldState == null)
      MDC.clear();
    else
      MDC.setContextMap(oldState);
  }

  private static Context current() {
    return PropagatedContext.getOrEmpty()
        .find(Context.class)
        .orElseThrow(() -> new IllegalStateException("Context not found"));
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(final String name, final T defaultValue) {
    if (!current().data().containsKey(name)) {
      return defaultValue;
    }
    return (T) current().data().get(name);
  }

  public static void set(final String name, final Object value) {
    current().data().put(name, value);
  }

}
