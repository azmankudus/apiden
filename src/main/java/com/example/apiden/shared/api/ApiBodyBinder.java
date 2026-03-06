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

import com.example.apiden.shared.infrastructure.Constant;

import java.util.Optional;

@Singleton
public final class ApiBodyBinder
    implements AnnotatedRequestArgumentBinder<ApiBody, Object> {

  private static final Logger logger = LoggerFactory.getLogger(ApiBodyBinder.class);

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
    return source.getAttribute(Constant.Attr.REQUEST_DATA, Object.class)
        .flatMap(data -> conversionService.convert(data, context))
        .map(val -> (BindingResult<Object>) () -> Optional.of(val))
        .orElse(ArgumentBinder.BindingResult.EMPTY);
  }
}
