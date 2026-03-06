package com.example.apiden.core.infra;

import java.util.Locale;

import io.micronaut.context.MessageSource;
import jakarta.inject.Singleton;

/**
 * Service for resolving localized messages from the application resource bundles.
 * 
 * <p>It utilizes Micronaut's {@link MessageSource} and automatically detects the current
 * language from the {@link Context}.</p>
 */
@Singleton
public final class Message {

  private final MessageSource messageSource;

  /**
   * Initializes the Message service.
   *
   * @param messageSource the underlying Micronaut message source
   */
  Message(final MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  /**
   * Resolves a simple message key.
   *
   * @param key the message key to resolve
   * @return the localized message or the key itself if not found
   */
  public String get(String key) {
    return messageSource.getMessage(key,
        Context.get(Constant.Attr.CONTEXT_LANGUAGE, Locale.ENGLISH)).orElse(key);
  }

  /**
   * Resolves a parameterized message key.
   *
   * @param key the message key to resolve
   * @param args the arguments to fill the placeholders (e.g., {0})
   * @return the localized and formatted message or the key itself if not found
   */
  public String get(String key, Object... args) {
    if (args == null || args.length == 0) {
      return get(key);
    }
    return messageSource.getMessage(key,
        Context.get(Constant.Attr.CONTEXT_LANGUAGE, Locale.ENGLISH),
        args).orElse(key);
  }
}
