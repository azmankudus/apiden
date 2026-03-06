package com.example.apiden.core.infra;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.value.PropertyResolver;
import jakarta.annotation.PostConstruct;
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
 * Manages application configuration with support for live updates.
 * 
 * <p>It acts as a caching layer over the standard Micronaut {@link PropertyResolver}
 * and selectively allows updates to pre-defined properties if live-update is enabled.</p>
 */
@Singleton
public final class ConfigManager {

  private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

  private final PropertyResolver propertyResolver;
  private final Message messages;
  private final Map<String, Object> configMap = new ConcurrentHashMap<>();
  private final boolean liveUpdateEnabled;
  private final Set<String> allowedProperties;

  /**
   * Constructs the ConfigManager.
   *
   * @param propertyResolver the underlying Micronaut property resolver
   * @param messages for localizing error messages
   * @param liveUpdateEnabled whether live updates are allowed globally
   * @param allowedListStr comma-separated list of property keys allowed for live update
   */
  ConfigManager(
      final PropertyResolver propertyResolver,
      final Message messages,
      @Value("${" + Constant.Config.LIVE_UPDATE_ENABLED + ":false}") final boolean liveUpdateEnabled,
      @Value("${" + Constant.Config.LIVE_UPDATE_LIST + ":}") final String allowedListStr) {

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
    logger.info("ConfigManager initialized: liveUpdateEnabled={}, allowedProperties={}",
        liveUpdateEnabled, allowedProperties);
  }

  /**
   * Warm-up cache with current values of allowed properties.
   */
  @PostConstruct
  void init() {
    for (final String key : allowedProperties) {
      Object value = propertyResolver.get(key, Object.class).orElse(null);
      if (value != null) {
        configMap.put(key, value);
      }
    }
  }

  /**
   * Retrieves a boolean property value with fallback.
   *
   * @param key the property key
   * @param defaultValue fallback value
   * @return the resolved boolean value
   */
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

  /**
   * Retrieves a generic property value.
   *
   * @param key the property key
   * @return the value or null
   */
  public Object get(final String key) {
    Object val = configMap.get(key);
    return val != null ? val : propertyResolver.get(key, Object.class).orElse(null);
  }

  /**
   * Returns all currently cached (live-updatable) configurations.
   *
   * @return immutable map of configurations
   */
  public Map<String, Object> getAll() {
    return Collections.unmodifiableMap(configMap);
  }

  /**
   * Updates a property value at runtime if allowed.
   *
   * @param key the property key to update
   * @param value the new value
   * @return the previous value
   * @throws IllegalAccessException if the update is not allowed
   */
  public Object update(final String key, final Object value) throws IllegalAccessException {
    if (!liveUpdateEnabled || !allowedProperties.contains(key)) {
      logger.warn("Unauthorized attempt to update property: {}", key);
      throw new IllegalAccessException(messages.get(Constant.Message.Core.ERR_FORBIDDEN));
    }

    Object oldValue = configMap.get(key);
    if (oldValue == null) {
      oldValue = propertyResolver.get(key, Object.class).orElse(null);
    }

    configMap.put(key, value);
    logger.info("Property updated: key={}, oldValue={}, newValue={}", key, oldValue, value);
    return oldValue;
  }
}
