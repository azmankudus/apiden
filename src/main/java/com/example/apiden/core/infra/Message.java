package com.example.apiden.core.infra;

import java.util.Locale;

import io.micronaut.context.MessageSource;
import jakarta.inject.Singleton;

@Singleton
public final class Message {

  private final MessageSource messageSource;

  Message(final MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  public String get(String key) {
    return messageSource.getMessage(key,
        Context.get(Constant.Attr.CONTEXT_LANGUAGE, Locale.ENGLISH)).orElse(key);
  }

  public String get(String key, Object... args) {
    if (args == null || args.length == 0) {
      return get(key);
    }
    return messageSource.getMessage(key,
        Context.get(Constant.Attr.CONTEXT_LANGUAGE, Locale.ENGLISH),
        args).orElse(key);
  }
}
