package com.example.apiden.shared.api;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * A custom argument binder that extracts the body from the standard API envelope stored in request attributes.
 */
@Singleton
public final class ApiBodyBinder
    implements AnnotatedRequestArgumentBinder<ApiBody, Object> {

  private static final Logger log = LoggerFactory.getLogger(ApiBodyBinder.class);

  private final ConversionService conversionService;

  @Inject
  public ApiBodyBinder(final ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  @Override
  public Class<ApiBody> getAnnotationType() {
    return ApiBody.class;
  }

  @SuppressWarnings("unchecked")
  @Override
  public BindingResult<Object> bind(
      final ArgumentConversionContext<Object> context,
      final HttpRequest<?> source) {

    log.trace("ApiBodyBinder binding for: {}", source.getUri());

    return source.getAttribute(ApiConstants.Attr.ENVELOPE, ApiObject.class).flatMap(apiObject -> {
      if (apiObject.client() != null && apiObject.client().request() != null) {
        Object body = apiObject.client().request().body();
        if (body != null) {
          return conversionService.convert(body, context);
        }
      }
      return Optional.empty();
    }).map(val -> (BindingResult<Object>) () -> Optional.of(val))
        .orElse(ArgumentBinder.BindingResult.EMPTY);
  }
}
