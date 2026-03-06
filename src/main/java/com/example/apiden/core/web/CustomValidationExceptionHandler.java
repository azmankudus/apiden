package com.example.apiden.core.web;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles validation errors (JSR-303/JSR-380) and converts them into the standard API envelope.
 */
@Produces
@Singleton
@Requires(classes = { ConstraintViolationException.class, ExceptionHandler.class })
final class CustomValidationExceptionHandler
    implements ExceptionHandler<ConstraintViolationException, HttpResponse<ResponseEnvelope>> {

  /**
   * Constructs a new CustomValidationExceptionHandler.
   */
  CustomValidationExceptionHandler() {
  }

  /**
   * Handles the ConstraintViolationException and returns a formatted response.
   *
   * @param request   The current HTTP request.
   * @param exception The caught constraint violation exception.
   * @return A standard API response containing validation errors.
   */
  @SuppressWarnings("rawtypes")
  @Override
  public HttpResponse<ResponseEnvelope> handle(final HttpRequest request,
      final ConstraintViolationException exception) {
    List<ResponseError> errors = new ArrayList<>();

    for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
      String path = violation.getPropertyPath().toString();
      String rawMessage = violation.getMessage();

      // Attempt to localize if message starts with { and ends with }
      String localizedMessage = rawMessage;

      errors.add(new ResponseError("00000400", localizedMessage, path));
    }

    ResponseEnvelope envelope = new ResponseEnvelope(null, errors, null);
    return HttpResponse.badRequest(envelope);
  }
}
