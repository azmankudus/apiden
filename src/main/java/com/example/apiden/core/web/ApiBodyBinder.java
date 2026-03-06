package com.example.apiden.core.web;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder;
import jakarta.inject.Singleton;

import com.example.apiden.core.infra.Constant;
import com.example.apiden.core.infra.Context;

import java.util.Optional;

@Singleton
final class ApiBodyBinder
    implements AnnotatedRequestArgumentBinder<ApiBody, Object> {

  private final ConversionService conversionService;

  ApiBodyBinder(final ConversionService conversionService) {
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
    Object data = Context.get(Constant.Attr.REQUEST_DATA, (Object) null);
    if (data == null) {
      return ArgumentBinder.BindingResult.EMPTY;
    }
    return conversionService.convert(data, context)
        .map(val -> (BindingResult<Object>) () -> Optional.of(val))
        .orElse(ArgumentBinder.BindingResult.EMPTY);
  }
}
