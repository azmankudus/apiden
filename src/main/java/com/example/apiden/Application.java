package com.example.apiden;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

/**
 * Entry point for the Apiden Web API application.
 * 
 * <p>This project serves as a foundational base for building enterprise-grade
 * Micronaut-based Web APIs with a standardized response envelope, comprehensive
 * logging, and modern infrastructure patterns.</p>
 */
@OpenAPIDefinition(info = @Info(title = "Apiden Foundation API", version = "0.1", description = "Foundational Web API project with standardized structures.", contact = @Contact(name = "API Support", email = "support@example.com"), license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html")))
public final class Application {

  /**
   * Private constructor to prevent instantiation from other classes.
   */
  Application() {
  }

  /**
   * Main entry point for the application.
   *
   * @param args command line arguments
   */
  public static void main(final String[] args) {
    Micronaut.build(args)
        .classes(Application.class)
        .banner(false)
        .start();
  }
}
