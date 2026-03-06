package com.example.apiden.core.web;

import io.micronaut.context.annotation.Primary;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Replaces the default Micronaut error response processor.
 * This ensures that even framework-level errors (like 404) are returned
 * using the standard {@link ResponseEnvelope}.
 */
@Produces
@Singleton
@Primary
final class CustomErrorProcessor implements ErrorResponseProcessor<ResponseEnvelope> {

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

    ResponseError error = new ResponseError(code, message, null);
    ResponseEnvelope envelope = new ResponseEnvelope(null, List.of(error), null);

    return response.body(envelope);
  }
}
