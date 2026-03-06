package com.example.apiden.shared.infrastructure;

import io.micronaut.core.propagation.PropagatedContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class MessageTest {

  @Inject
  Message message;

  @Test
  void testResolveDefault() {
    Map<String, Object> data = new ConcurrentHashMap<>();
    data.put(Constant.Attr.CONTEXT_LANGUAGE, Locale.ENGLISH);
    Context context = new Context(data);

    try (PropagatedContext.Scope ignored = PropagatedContext.getOrEmpty().plus(context).propagate()) {
      String msg = message.get("msg.success");
      assertEquals("Success", msg);
    }
  }

  @Test
  void testResolveMalay() {
    Map<String, Object> data = new ConcurrentHashMap<>();
    data.put(Constant.Attr.CONTEXT_LANGUAGE, Locale.of("ms"));
    Context context = new Context(data);

    try (PropagatedContext.Scope ignored = PropagatedContext.getOrEmpty().plus(context).propagate()) {
      String msg = message.get("msg.success");
      assertEquals("Berjaya", msg);
    }
  }

  @Test
  void testResolveMalayLabel() {
    Map<String, Object> data = new ConcurrentHashMap<>();
    data.put(Constant.Attr.CONTEXT_LANGUAGE, Locale.of("ms"));
    Context context = new Context(data);

    try (PropagatedContext.Scope ignored = PropagatedContext.getOrEmpty().plus(context).propagate()) {
      String msg = message.get("label.hello.world");
      assertEquals("Hai Dunia", msg);
    }
  }

  @Test
  void testResolveNotFound() {
    Map<String, Object> data = new ConcurrentHashMap<>();
    data.put(Constant.Attr.CONTEXT_LANGUAGE, Locale.ENGLISH);
    Context context = new Context(data);

    try (PropagatedContext.Scope ignored = PropagatedContext.getOrEmpty().plus(context).propagate()) {
      String key = "non.existent.key";
      String msg = message.get(key);
      assertEquals(key, msg);
    }
  }
}
