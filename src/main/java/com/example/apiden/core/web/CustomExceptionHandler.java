package com.example.apiden.core.web;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;

import com.example.apiden.core.infra.ApplicationError;
import com.example.apiden.core.infra.ConfigManager;
import com.example.apiden.core.infra.Constant;
import com.example.apiden.core.infra.Message;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Catch-all exception handler that translates unhandled exceptions and {@link ApplicationError}s
 * into the standard API {@link ResponseEnvelope}.
 */
@Produces
@Singleton
@Requires(classes = { Exception.class, ExceptionHandler.class })
final class CustomExceptionHandler implements ExceptionHandler<Exception, HttpResponse<ResponseEnvelope>> {

  private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

  private final ConfigManager config;
  private final Message messages;

  CustomExceptionHandler(final ConfigManager config, final Message messages) {
    this.config = config;
    this.messages = messages;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public HttpResponse<ResponseEnvelope> handle(final HttpRequest request, final Exception exception) {
    logger.error("Unhandled exception occurred: {}", exception.getMessage(), exception);

    ResponseError error;
    if (exception instanceof ApplicationError appError) {
      String ec = appError.getCode() != null ? appError.getCode() : Constant.Code.ERROR;
      String em = messages.get(ec);
      Object detail = appError.getArgs() != null ? messages.get(ec, appError.getArgs().toArray()) : null;
      error = new ResponseError(ec, em, detail);
    } else {
      String ec = Constant.Code.ERROR;
      String em = exception.getMessage() != null ? exception.getMessage()
          : messages.get(Constant.Message.Core.ERR_SERVER);
      error = new ResponseError(ec, em, null);
    }

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
