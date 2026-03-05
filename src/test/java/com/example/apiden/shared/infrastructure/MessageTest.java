package com.example.apiden.shared.infrastructure;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import com.example.apiden.shared.api.ApiConstants;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class MessageTest {

  @Inject
  Message message;

  @Inject
  Context context;

  @Test
  void testResolveDefault() {
    // Should resolve to English if system default is en
    String msg = message.get("msg.success");
    assertEquals("Success", msg);
  }

  @Test
  void testResolveMalay() {
    context.set(ApiConstants.Attr.CONTEXT_LANGUAGE, Locale.of("ms"));
    String msg = message.get("msg.success");
    assertEquals("Berjaya", msg);
  }

  @Test
  void testResolveWithParameters() {
    context.set(ApiConstants.Attr.CONTEXT_LANGUAGE, Locale.of("ms"));
    String msg = message.get("msg.invalid.log.level");
    assertEquals("Aras log tidak sah: SUPER_TRACE", msg);
  }

  @Test
  void testResolveNotFound() {
    String key = "non.existent.key";
    String msg = message.get(key);
    assertEquals(key, msg);
  }
}
