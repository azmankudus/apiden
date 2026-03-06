package com.example.apiden.shared.infrastructure;

import java.util.Locale;

import io.micronaut.context.MessageSource;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public final class Message {

  private final MessageSource messageSource;

  Message(final MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public String get(String key) {
    return messageSource.getMessage(key, key,
        Context.get(Constant.Attr.CONTEXT_LANGUAGE, Locale.ENGLISH));
  }
}
