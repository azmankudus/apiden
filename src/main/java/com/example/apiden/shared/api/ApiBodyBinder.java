package com.example.apiden.shared.api;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * A custom argument binder that extracts the body from the standard API envelope.
 * It primarily looks for data within 'client.request.body' in the incoming JSON.
 */
@Singleton
public final class ApiBodyBinder
    implements AnnotatedRequestArgumentBinder<ApiBody, Object> {

  /** logger instance for ApiBodyBinder. */
  private static final Logger log = LoggerFactory.getLogger(ApiBodyBinder.class);

  private final JsonMapper jsonMapper;
  private final ConversionService conversionService;

  /**
   * Constructs the binder with required Micronaut services.
   *
   * @param jsonMapper The Micronaut JSON mapper for parsing message bodies.
   * @param conversionService The Micronaut conversion service for type binding.
   */
  ApiBodyBinder(final JsonMapper jsonMapper, final ConversionService conversionService) {
    log.debug("Initializing ApiBodyBinder with JsonMapper and ConversionService.");
    this.jsonMapper = jsonMapper;
    this.conversionService = conversionService;
  }

  /**
   * Returns the annotation type supported by this binder.
   *
   * @return The ApiBody class.
   */
  @Override
  public Class<ApiBody> getAnnotationType() {
    return ApiBody.class;
  }

  /**
   * Binds the request attribute or body content to the annotated parameter.
   *
   * @param context The conversion context.
   * @param source The incoming HTTP request.
   * @return The result of the binding operation.
   */
  @SuppressWarnings("unchecked")
  @Override
  public BindingResult<Object> bind(
      final ArgumentConversionContext<Object> context,
      final HttpRequest<?> source) {

    log.trace("Attempting to bind @ApiBody for request: {} {}", source.getMethod(), source.getUri());

    // First, try to retrieve the pre-parsed ApiObject from request attributes
    final ApiObject apiObject = source.getAttribute(ApiFilter.ENVELOPE_ATTR, ApiObject.class).orElse(null);

    if (apiObject != null && apiObject.client() != null && apiObject.client().request() != null) {
      final Object businessBody = apiObject.client().request().body();
      if (businessBody != null) {
        log.debug("Found pre-parsed business body in ApiObject attribute.");
        return () -> conversionService.convert(businessBody, context);
      }
    }

    log.debug("ApiObject attribute missing or incomplete. Attempting to parse request body manually.");

    // Fallback: Manually parse the request body bytes if attribute search failed
    try {
      final Optional<byte[]> bodyBytes = source.getBody(byte[].class);
      if (bodyBytes.isPresent()) {
        log.trace("Parsing request body bytes (length: {}).", bodyBytes.get().length);

        final JsonNode root = jsonMapper.readValue(bodyBytes.get(), JsonNode.class);
        final JsonNode client = root.get("client");
        if (client != null) {
          final JsonNode requestNode = client.get("request");
          if (requestNode != null) {
            final JsonNode bodyNode = requestNode.get("body");
            if (bodyNode != null) {
              log.debug("Extracted body node from JSON structure: client.request.body");
              return () -> conversionService.convert(bodyNode, context);
            }
          }
        }
        log.warn("Target body node 'client.request.body' not found in JSON structure.");
      } else {
        log.debug("No request body found to bind.");
      }
    } catch (final IOException e) {
      log.error("Failed to parse request body during binding: {}", e.getMessage(), e);
    }

    log.trace("Binding result is empty.");
    return BindingResult.EMPTY;
  }
}
