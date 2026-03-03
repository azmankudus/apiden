package com.example.apiden.module.hello;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import com.example.apiden.shared.api.ApiBody;
import com.example.apiden.shared.api.ApiException;
import com.example.apiden.shared.api.ResponseBody;
import com.example.apiden.shared.api.ResponseStatus;

/**
 * Controller for handling 'hello' related API requests.
 * Provides endpoints for greeting messages, echoing requests, and simulated error generation.
 */
@Controller("/hello")
public final class HelloController {

  /** logger instance for HelloController. */
  private static final Logger log = LoggerFactory.getLogger(HelloController.class);

  /**
   * Default constructor for HelloController.
   * Annotated with package-level access control.
   */
  HelloController() {
    log.debug("Initializing HelloController instance.");
  }

  /**
   * Endpoint for greeting the user with a default 'Hello World' message.
   *
   * @return A MutableHttpResponse containing a success message and 'Hello World' data.
   */
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public MutableHttpResponse<ResponseBody> index() {
    // Log the request and execution flow
    log.info("Received request for GET /hello");
    log.debug("Preparing successful response for index endpoint.");

    return HttpResponse.ok(new ResponseBody(ResponseStatus.SUCCESS, "0", "Success", Map.of("message", "Hello World")));
  }

  /**
   * Endpoint for echoing a sentiment provided in the request body, reversing the message.
   *
   * @param hello The Hello message object provided in the request body.
   * @return A MutableHttpResponse containing the reversed message or a bad request response.
   */
  @Post
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public MutableHttpResponse<ResponseBody> echo(@ApiBody final Hello hello) {
    // Log the entry into the echo endpoint
    log.info("Received request for POST /hello for echo processing.");

    // Validate the input message
    if (hello == null || hello.message() == null) {
      log.warn("Null or empty Hello message received in echo request.");
      return HttpResponse.badRequest(new ResponseBody(ResponseStatus.ERROR, "400", "Message is required", null));
    }

    log.debug("Input message received: {}", hello.message());
    log.trace("Full details of the received object for echo: {}", hello);

    // Process the message (simulated work)
    final String reversedMessage = new StringBuilder(hello.message()).reverse().toString();
    final Hello helloResponse = new Hello(reversedMessage);

    log.trace("Reversed the input message: {} -> {}", hello.message(), reversedMessage);
    log.info("Echo response successfully prepared.");

    return HttpResponse.ok(new ResponseBody(ResponseStatus.SUCCESS, "0", "Success", helloResponse));
  }

  /**
   * Endpoint for simulating an error, primarily for testing ApiException handling.
   *
   * @return This endpoint always throws an ApiException.
   * @throws ApiException To simulate custom API errors.
   */
  @Get("/error")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<ResponseBody> throwError() throws ApiException {
    // Log the intent to throw an error for tracing and debugging purposes
    log.info("Received request for GET /hello/error; triggered a simulated ApiException.");
    log.trace("Throwing a simulated error to demonstrate global exception handling.");

    throw new ApiException("ERR-100", "This is a simulated API error from a GET request.");
  }

  /**
   * Endpoint for simulating an error, primarily for testing ApiException handling with a POST request.
   *
   * @param hello A Hello object provided in the request body to simulate a real scenario.
   * @return This endpoint always throws an ApiException.
   * @throws ApiException To simulate custom API errors.
   */
  @Post("/error")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<ResponseBody> throwError(@ApiBody final Hello hello) throws ApiException {
    // Log the intent to throw an error for tracing and debugging purposes
    log.info("Received request for POST /hello/error; triggered a simulated ApiException.");
    log.debug("Input message received for error scenario: {}", hello != null ? hello.message() : "none");

    throw new ApiException("ERR-100", "This is a simulated API error from a POST request.");
  }
}
