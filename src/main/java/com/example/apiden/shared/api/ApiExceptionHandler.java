package com.example.apiden.shared.api;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import io.micronaut.context.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for handling {@link ApiException} thrown during request processing.
 * Formats the exception into a standard {@link ResponseBody} structure.
 */
@Produces
@Singleton
@Requires(classes = { ApiException.class, ExceptionHandler.class })
public final class ApiExceptionHandler implements ExceptionHandler<ApiException, HttpResponse<ResponseBody>> {

  /** logger instance for ApiExceptionHandler. */
  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  private final boolean includeStacktrace;

  /**
   * Constructs the exception handler with application configuration.
   *
   * @param includeStacktrace Flag to determine if stacktraces should be included in the response.
   */
  ApiExceptionHandler(@Value("${application.api.envelope.include-stacktrace:true}") final boolean includeStacktrace) {
    log.debug("Initializing ApiExceptionHandler with includeStacktrace={}", includeStacktrace);
    this.includeStacktrace = includeStacktrace;
  }

  /**
   * Handles the ApiException and converts it to a standard HTTP response.
   *
   * @param request The original HTTP request.
   * @param exception The caught ApiException.
   * @return An HTTP response with a formatted Error body.
   */
  @SuppressWarnings("rawtypes")
  @Override
  public HttpResponse<ResponseBody> handle(final HttpRequest request, final ApiException exception) {
    log.error("Handling ApiException: code={}, message={}", exception.getCode(), exception.getMessage());
    log.trace("Processing exception for request: {} {}", request.getMethod(), request.getUri());

    String stackTrace = null;
    if (includeStacktrace) {
      log.debug("Formatting stacktrace for inclusion in response.");
      try (StringWriter stringWriter = new StringWriter();
          PrintWriter printWriter = new PrintWriter(stringWriter)) {
        exception.printStackTrace(printWriter);
        stackTrace = stringWriter.toString();

        // Truncate long stacktraces for response brevity
        if (stackTrace != null && stackTrace.length() > 1000) {
          log.trace("Truncating stacktrace length from {} to 1000 characters.", stackTrace.length());
          stackTrace = stackTrace.substring(0, 1000);
        }
      } catch (final Exception e) {
        log.warn("An error occurred while attempting to capture stacktrace: {}", e.getMessage());
      }
    } else {
      log.trace("Stacktrace inclusion is disabled by configuration.");
    }

    // Build the exception details map
    final Map<String, Object> exceptionContent = new HashMap<>();
    exceptionContent.put("message",
        exception.getMessage() != null ? exception.getMessage() : "No error message provided");

    if (includeStacktrace && stackTrace != null) {
      exceptionContent.put("stacktrace", stackTrace);
    }

    final Map<String, Object> exceptionData = Map.of("exception", exceptionContent);

    // Create the final response body
    final ResponseBody responseBody = new ResponseBody(
        ResponseStatus.ERROR,
        exception.getCode() != null ? exception.getCode() : "500",
        exception.getMessage() != null ? exception.getMessage() : "Server Error",
        exceptionData);

    log.info("API Exception handled. Returning status code: {} (code: {})",
        exception.getCode(), responseBody.code());

    return HttpResponse.serverError(responseBody);
  }
}
