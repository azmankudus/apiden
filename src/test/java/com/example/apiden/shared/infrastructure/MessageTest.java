package com.example.apiden.shared.infrastructure;

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

  private void runWithLanguage(Locale locale, Runnable task) {
    Map<String, Object> data = new ConcurrentHashMap<>();
    data.put(Constant.Attr.CONTEXT_LANGUAGE, locale);
    Context.init(data);
    try {
      task.run();
    } finally {
      Context.destroy();
    }
  }

  @Test
  void testResolveDefault() {
    runWithLanguage(Locale.ENGLISH, () -> {
      String msg = message.get("msg.success");
      assertEquals("Success", msg);
    });
  }

  @Test
  void testResolveMalay() {
    runWithLanguage(Locale.of("ms"), () -> {
      String msg = message.get("msg.success");
      assertEquals("Berjaya", msg);
    });
  }

  @Test
  void testResolveMalayLabel() {
    runWithLanguage(Locale.of("ms"), () -> {
      String msg = message.get("label.hello.world");
      assertEquals("Hai Dunia", msg);
    });
  }

  @Test
  void testResolveNotFound() {
    runWithLanguage(Locale.ENGLISH, () -> {
      String key = "non.existent.key";
      String msg = message.get(key);
      assertEquals(key, msg);
    });
  }
}
