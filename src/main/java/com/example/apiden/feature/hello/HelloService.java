package com.example.apiden.feature.hello;

import com.example.apiden.core.infra.Constant;
import com.example.apiden.core.infra.Message;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.resilience4j.retry.annotation.Retry;

/**
 * Service for greeting-related business logic.
 */
@Singleton
public final class HelloService {

  private static final Logger logger = LoggerFactory.getLogger(HelloService.class);
  private final Message messages;

  HelloService(final Message messages) {
    this.messages = messages;
  }

  /**
   * Generates a standard greeting message.
   *
   * @return a localized greeting
   */
  @Retry(name = "helloService")
  public String getGreeting() {
    logger.debug("Resolving greeting message");
    return messages.get(Constant.Message.Hello.TXT_HELLO);
  }

  /**
   * Reverses the message from a Hello object.
   *
   * @param hello the input object
   * @return the reversed greeting
   */
  public String reverseGreeting(final Hello hello) {
    logger.debug("Reversing message: {}", hello.message());
    if (hello.message() == null) {
      return null;
    }
    return new StringBuilder(hello.message()).reverse().toString();
  }
}
