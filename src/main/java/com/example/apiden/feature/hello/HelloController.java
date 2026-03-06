package com.example.apiden.feature.hello;

import com.example.apiden.core.web.ApiBody;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Consumes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for Hello World demonstrations.
 */
@Tag(name = "Hello", description = "Simple greeting endpoints")
@Controller("/hello")
public final class HelloController {

  private static final Logger logger = LoggerFactory.getLogger(HelloController.class);
  private final HelloService helloService;

  /**
   * Constructs a new HelloController.
   *
   * @param helloService The service for greeting logic.
   */
  HelloController(final HelloService helloService) {
    this.helloService = helloService;
  }

  /**
   * Returns a standard greeting.
   *
   * @return a Hello object with a localized greeting message
   */
  @Operation(summary = "Get greeting", description = "Returns a localized 'Hello World' message.")
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public Hello index() {
    return new Hello(helloService.getGreeting());
  }

  /**
   * Echoes back a reversed greeting message.
   *
   * @param hello the incoming greeting object
   * @return a Hello object with the message reversed
   */
  @Operation(summary = "Reverse greeting", description = "Takes a message and returns its reverse.")
  @Post
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Hello echo(@ApiBody final Hello hello) {
    logger.info("Received echo request with message: {}", hello.message());
    return new Hello(helloService.reverseGreeting(hello));
  }
}
