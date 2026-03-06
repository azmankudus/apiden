package com.example.apiden.feature.management;

import com.example.apiden.core.infra.ApplicationError;
import com.example.apiden.core.web.ApiBody;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for testing and demonstrating error handling.
 */
@Tag(name = "Management", description = "System management and configuration")
@Controller("/error")
public final class ErrorController {

  /**
   * Constructs a new ErrorController.
   */
  ErrorController() {
  }

  /**
   * Throws a localized ApplicationError.
   *
   * @return Never returns normally.
   * @throws Exception always thrown for demonstration.
   */
  @Operation(summary = "Trigger ApplicationError", description = "Forcedly throws an ApplicationError to test handler.")
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public String index() throws Exception {
    throw new ApplicationError("ERR-04000001"); // Using a dummy but well-formatted code
  }

  /**
   * Throws a generic Exception.
   *
   * @param body Any request body.
   * @return Never returns normally.
   * @throws Exception always thrown for demonstration.
   */
  @Operation(summary = "Trigger Generic Exception", description = "Forcedly throws a raw Exception to test catch-all handler.")
  @Post
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String echo(@ApiBody final String body) throws Exception {
    throw new Exception("Unexpected internal error occurred during test.");
  }
}
