package com.example.apiden.shared.infrastructure;

import io.micronaut.context.MessageSource;
import com.example.apiden.shared.api.ApiConstants;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Helper component to simplify message resolution from the resource bundle.
 * Automatically resolves the locale from the current request context if not explicitly provided.
 */
@Singleton
public final class Message {

  private final MessageSource messageSource;

  @Inject
  Message(final MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public String get(String key) {
    return messageSource.getMessage(key, key,
        Context.get(ApiConstants.Attr.CONTEXT_LANGUAGE, ApiConstants.Default.DEFAULT_LOCALE));
  }
}
