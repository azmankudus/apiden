package com.example.apiden.shared.infrastructure;

import com.example.apiden.shared.api.ApiConstants;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.value.PropertyResolver;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Centralized configuration service for Apiden.
 * Manages runtime properties in a Map and enforces live-update rules dynamically.
 */
@Singleton
public final class ConfigManager {

  private static final Logger log = LoggerFactory.getLogger(ConfigManager.class);

  private final PropertyResolver propertyResolver;
  private final Message messages;
  private final Map<String, Object> configMap = new ConcurrentHashMap<>();
  private final boolean liveUpdateEnabled;
  private final Set<String> allowedProperties;

  @Inject
  ConfigManager(
      final PropertyResolver propertyResolver,
      final Message messages,
      @Value("${" + ApiConstants.Config.LIVE_UPDATE_ENABLED + ":false}") final boolean liveUpdateEnabled,
      @Value("${" + ApiConstants.Config.LIVE_UPDATE_LIST + ":}") final String allowedListStr) {

    this.propertyResolver = propertyResolver;
    this.messages = messages;
    this.liveUpdateEnabled = liveUpdateEnabled;

    final List<String> allowedList = (allowedListStr == null || allowedListStr.isBlank())
        ? Collections.emptyList()
        : Arrays.stream(allowedListStr.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());

    this.allowedProperties = Collections.unmodifiableSet(new HashSet<>(allowedList));
    log.info("ConfigManager initialized: liveUpdateEnabled={}, allowedProperties={}", liveUpdateEnabled,
        allowedProperties);
  }

  @PostConstruct
  void init() {
    log.debug("Populating configMap from propertyResolver for {} properties.", allowedProperties.size());
    for (final String key : allowedProperties) {
      // Use Boolean explicitly if we expect boolean, or Object if generic
      Object value = propertyResolver.get(key, Object.class).orElse(null);
      if (value != null) {
        log.trace("Config init: {} = {}", key, value);
        configMap.put(key, value);
      }
    }
  }

  public boolean getBoolean(final String key, final boolean defaultValue) {
    Object val = configMap.get(key);
    if (val == null) {
      return propertyResolver.get(key, Boolean.class).orElse(defaultValue);
    }
    if (val instanceof Boolean b) {
      return b;
    }
    if (val instanceof String s) {
      return Boolean.parseBoolean(s);
    }
    return defaultValue;
  }

  public Object get(final String key) {
    Object val = configMap.get(key);
    return val != null ? val : propertyResolver.get(key, Object.class).orElse(null);
  }

  public Map<String, Object> getAll() {
    return Collections.unmodifiableMap(configMap);
  }

  /**
   * Updates a single configuration property.
   *
   * @param key   The property key.
   * @param value The new value.
   * @return The old value if successful.
   * @throws IllegalAccessException if unauthorized or disabled.
   */
  public Object update(final String key, final Object value) throws IllegalAccessException {
    if (!liveUpdateEnabled || !allowedProperties.contains(key)) {
      log.warn("Unauthorized update attempt: key={}, enabled={}, allowed={}", key, liveUpdateEnabled,
          allowedProperties.contains(key));
      throw new IllegalAccessException(messages.get(ApiConstants.Msg.FORBIDDEN));
    }

    Object oldValue = configMap.get(key);
    if (oldValue == null) {
      oldValue = propertyResolver.get(key, Object.class).orElse(null);
    }

    log.info("Config update: {} ({} -> {})", key, oldValue, value);
    configMap.put(key, value);
    return oldValue;
  }
}
