package com.example.apiden;

import io.micronaut.runtime.Micronaut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Application {

  private static final Logger logger = LoggerFactory.getLogger(Application.class);

  Application() {
  }

  public static void main(final String[] args) throws Exception {
    try {
      Micronaut.build(args)
          .classes(Application.class)
          .banner(false)
          .start();
    } catch (final Exception e) {
      throw e;
    }

  }
}
