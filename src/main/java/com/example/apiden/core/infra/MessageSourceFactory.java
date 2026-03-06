package com.example.apiden.core.infra;

import io.micronaut.context.MessageSource;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.i18n.ResourceBundleMessageSource;
import jakarta.inject.Singleton;

/**
 * Factory for creating the MessageSource bean if not auto-configured.
 * 
 * <p>This ensures that the application can resolve localized strings from the
 * 'messages' resource bundles.</p>
 */
@Factory
final class MessageSourceFactory {

  /**
   * Constructs a new MessageSourceFactory.
   */
  MessageSourceFactory() {
  }

  /**
   * Provides a ResourceBundleMessageSource for the 'messages' bundle.
   *
   * @return The message source bean.
   */
  @Bean
  @Singleton
  public MessageSource messageSource() {
    return new ResourceBundleMessageSource("messages");
  }
}
