package com.example.apiden.core.web;

import io.micronaut.context.annotation.Primary;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Custom processor that intercepts framework-level error responses and formats them
 * into the standardized API {@link ResponseEnvelope}.
 */
@Produces
@Singleton
@Primary
final class CustomErrorProcessor implements ErrorResponseProcessor<ResponseEnvelope> {

  private static final Logger logger = LoggerFactory.getLogger(CustomErrorProcessor.class);

  CustomErrorProcessor() {
  }

  @Override
  public MutableHttpResponse<ResponseEnvelope> processResponse(
      final ErrorContext errorContext,
      final MutableHttpResponse<?> response) {

    String code = String.format("00000%03d", response.status().getCode());
    String message = response.status().getReason();

    if (message == null || message.isBlank()) {
      message = "Error " + code;
    }

    logger.debug("Processing framework error: status={}, message={}", response.status().getCode(), message);

    ResponseError error = new ResponseError(code, message, null);
    ResponseEnvelope envelope = new ResponseEnvelope(null, List.of(error), null);

    return response.body(envelope);
  }
}
