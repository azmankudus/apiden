package com.example.apiden.shared.infrastructure;

import io.micronaut.core.propagation.PropagatedContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class ConfigManagerTest {

  @Inject
  ConfigManager configManager;

  /**
   * Helper to run code within a propagated context (required by Message.get()).
   */
  private void withContext(Runnable action) {
    Map<String, Object> data = new ConcurrentHashMap<>();
    data.put(Constant.Attr.CONTEXT_LANGUAGE, Locale.ENGLISH);
    Context context = new Context(data);
    try (PropagatedContext.Scope ignored = PropagatedContext.getOrEmpty().plus(context).propagate()) {
      action.run();
    }
  }

  @Test
  void testGetConfig() {
    Object val = configManager.get("application.api.response.include-metadata");
    assertNotNull(val, "include-metadata config property should exist");
  }

  @Test
  void testGetAll() {
    Map<String, Object> all = configManager.getAll();
    assertFalse(all.isEmpty());
    assertTrue(all.containsKey("application.api.response.include-metadata"));
  }

  @Test
  void testUpdateAllowed() {
    withContext(() -> {
      try {
        String key = "application.api.response.include-metadata";
        Object oldVal = configManager.get(key);

        Object previous = configManager.update(key, false);
        assertEquals(oldVal, previous);
        assertEquals(false, configManager.get(key));

        // Restore
        configManager.update(key, oldVal);
      } catch (IllegalAccessException e) {
        fail("Should not throw IllegalAccessException for allowed property: " + e.getMessage());
      }
    });
  }

  @Test
  void testUpdateForbidden() {
    withContext(() -> {
      String key = "non.allowed.property";
      assertThrows(IllegalAccessException.class, () -> {
        configManager.update(key, "some value");
      });
    });
  }
}
