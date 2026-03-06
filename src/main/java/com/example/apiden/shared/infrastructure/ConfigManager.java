package com.example.apiden.shared.infrastructure;

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

@Singleton
public final class ConfigManager {

  private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);

  private final PropertyResolver propertyResolver;
  private final Message messages;
  private final Map<String, Object> configMap = new ConcurrentHashMap<>();
  private final boolean liveUpdateEnabled;
  private final Set<String> allowedProperties;

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
  }

  @PostConstruct
  void init() {
    for (final String key : allowedProperties) {
      // Use Boolean explicitly if we expect boolean, or Object if generic
      Object value = propertyResolver.get(key, Object.class).orElse(null);
      if (value != null) {
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

  public Object update(final String key, final Object value) throws IllegalAccessException {
    if (!liveUpdateEnabled || !allowedProperties.contains(key)) {
      throw new IllegalAccessException(messages.get(Constant.Msg.FORBIDDEN));
    }

    Object oldValue = configMap.get(key);
    if (oldValue == null) {
      oldValue = propertyResolver.get(key, Object.class).orElse(null);
    }

    configMap.put(key, value);
    return oldValue;
  }
}
