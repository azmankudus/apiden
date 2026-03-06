package com.example.apiden.shared.api;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.apiden.shared.infrastructure.ApplicationError;
import com.example.apiden.shared.infrastructure.ConfigManager;
import com.example.apiden.shared.infrastructure.Constant;
import com.example.apiden.shared.infrastructure.Message;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Produces
@Singleton
@Requires(classes = { Exception.class, ExceptionHandler.class })
public final class ApiExceptionHandler implements ExceptionHandler<Exception, HttpResponse<ResponseEnvelope>> {

  private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

  private final ConfigManager config;
  private final Message messages;

  @Inject
  ApiExceptionHandler(final ConfigManager config, final Message messages) {
    this.config = config;
    this.messages = messages;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public HttpResponse<ResponseEnvelope> handle(final HttpRequest request, final Exception exception) {

    // Determine error code and message
    String code;
    String errorMessage;

    if (exception instanceof ApplicationError appError) {
      code = appError.getCode() != null ? appError.getCode() : Constant.Code.BAD_REQUEST;
      errorMessage = appError.getMessage() != null ? appError.getMessage()
          : messages.get(appError.getCode());
    } else {
      code = Constant.Code.ERROR;
      errorMessage = exception.getMessage() != null ? exception.getMessage()
          : messages.get(Constant.Msg.SERVER_ERROR);
    }

    // Build the error entry
    ApiError error = new ApiError(code, errorMessage, null);

    // Build meta with optional stacktrace
    Map<String, Object> meta = null;
    boolean includeStacktrace = config.getBoolean(Constant.Config.INCLUDE_STACKTRACE, true);
    if (includeStacktrace) {
      String stackTrace = captureStackTrace(exception);
      if (stackTrace != null) {
        meta = new LinkedHashMap<>();
        meta.put(Constant.Meta.SERVER_RESPONSE_EXCEPTION_STACKTRACE, stackTrace);
      }
    }

    ResponseEnvelope envelope = new ResponseEnvelope(null, List.of(error), meta);

    if (exception instanceof ApplicationError) {
      return HttpResponse.badRequest(envelope);
    }

    return HttpResponse.serverError(envelope);
  }

  private String captureStackTrace(final Exception exception) {
    try (StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw)) {
      exception.printStackTrace(pw);
      String trace = sw.toString();
      if (trace != null && trace.length() > 1000) {
        trace = trace.substring(0, 1000);
      }
      return trace;
    } catch (Exception e) {
      return null;
    }
  }
}
