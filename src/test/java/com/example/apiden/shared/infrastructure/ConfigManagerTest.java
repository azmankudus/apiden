package com.example.apiden.shared.infrastructure;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class ConfigManagerTest {

  @Inject
  ConfigManager configManager;

  @Inject
  io.micronaut.context.ApplicationContext context;

  @Test
  void testGetConfig() {
    System.out.println("DEBUG ALL PROP: "
        + context.getProperty("application.configuration.live-update.list", String.class).orElse("MISSING"));
    Object val = configManager.get("application.api.envelope.include-client-headers");
    assertNotNull(val);
    assertTrue(val instanceof Boolean);
  }

  @Test
  void testGetAll() {
    Map<String, Object> all = configManager.getAll();
    assertFalse(all.isEmpty());
    assertTrue(all.containsKey("application.api.envelope.include-client-headers"));
  }

  @Test
  void testUpdateAllowed() throws IllegalAccessException {
    String key = "application.api.envelope.include-client-headers";
    Object oldVal = configManager.get(key);

    Object previous = configManager.update(key, false);
    assertEquals(oldVal, previous);
    assertEquals(false, configManager.get(key));

    // Restore
    configManager.update(key, oldVal);
  }

  @Test
  void testUpdateForbidden() {
    String key = "non.allowed.property";
    assertThrows(IllegalAccessException.class, () -> {
      configManager.update(key, "some value");
    });
  }
}
