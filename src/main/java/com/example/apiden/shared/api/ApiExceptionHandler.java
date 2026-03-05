package com.example.apiden.shared.api;

import com.example.apiden.shared.infrastructure.ConfigManager;
import com.example.apiden.shared.infrastructure.Message;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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

  private final ConfigManager config;
  private final Message messages;

  /**
   * Constructs the exception handler with application configuration.
   *
   * @param config Centralized configuration service for Apiden.
   * @param messages Helper for i18n message resolution.
   */
  @Inject
  ApiExceptionHandler(final ConfigManager config, final Message messages) {
    log.debug("Initializing ApiExceptionHandler instance.");
    this.config = config;
    this.messages = messages;
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

    boolean includeStacktrace = config.getBoolean(ApiConstants.Config.INCLUDE_STACKTRACE, true);

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
    exceptionContent.put(ApiConstants.Key.MESSAGE,
        exception.getMessage() != null ? exception.getMessage()
            : messages.get(ApiConstants.Msg.NO_ERROR_MSG));

    if (includeStacktrace && stackTrace != null) {
      exceptionContent.put(ApiConstants.Key.STACKTRACE, stackTrace);
    }

    final Map<String, Object> exceptionData = Map.of(ApiConstants.Key.EXCEPTION, exceptionContent);

    // Create the final response body
    final ResponseBody responseBody = new ResponseBody(
        ResponseStatus.ERROR,
        exception.getCode() != null ? exception.getCode() : ApiConstants.Code.ERROR,
        exception.getMessage() != null ? exception.getMessage()
            : messages.get(ApiConstants.Msg.SERVER_ERROR),
        exceptionData);

    log.info("API Exception handled. Returning status code: {} (code: {})",
        exception.getCode(), responseBody.code());

    return HttpResponse.serverError(responseBody);
  }
}
