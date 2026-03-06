package com.example.apiden;

import io.micronaut.runtime.Micronaut;

public final class Application {

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
