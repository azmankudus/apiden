package com.example.apiden.shared.api;

import io.micronaut.core.annotation.Order;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Filter;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import io.micronaut.core.propagation.MutablePropagatedContext;

import com.example.apiden.shared.infrastructure.Constant;
import com.example.apiden.shared.infrastructure.ConfigManager;
import com.example.apiden.shared.infrastructure.Context;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ServerFilter(Filter.MATCH_ALL_PATTERN)
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class ApiFilter {

  private static final Logger logger = LoggerFactory.getLogger(ApiFilter.class);

  private final JsonMapper jsonMapper;
  private final ConfigManager config;

  @Inject
  ApiFilter(final JsonMapper jsonMapper, final ConfigManager config) {
    this.jsonMapper = jsonMapper;
    this.config = config;
  }

  @RequestFilter
  public void filterRequest(final HttpRequest<?> request, final MutablePropagatedContext mutableContext,
      @Nullable @Body String body) {

    request.setAttribute(Constant.Attr.TIMESTAMP, OffsetDateTime.now());

    Object requestData = null;
    String clientTraceId = null;
    String clientTimestamp = null;

    if (body != null && !body.isEmpty()) {
      try {
        JsonNode root = jsonMapper.readValue(body, JsonNode.class);
        if (root.isObject()) {
          JsonNode dataNode = root.get(Constant.Envelope.DATA);
          if (dataNode != null) {
            requestData = jsonMapper.readValue(jsonMapper.writeValueAsBytes(dataNode), Object.class);
          }
          JsonNode metaNode = root.get(Constant.Envelope.META);
          if (metaNode != null && metaNode.isObject()) {
            JsonNode traceNode = metaNode.get(Constant.Meta.CLIENT_TRACE_ID);
            if (traceNode != null && traceNode.isString()) {
              clientTraceId = traceNode.getStringValue();
            }
            JsonNode tsNode = metaNode.get(Constant.Meta.CLIENT_REQUEST_TIMESTAMP);
            if (tsNode != null && tsNode.isString()) {
              clientTimestamp = tsNode.getStringValue();
            }
          }
        }
      } catch (Exception e) {
        logger.trace("Failed to parse body as API envelope: {}", e.getMessage());
      }
    }

    if (requestData != null) {
      request.setAttribute(Constant.Attr.REQUEST_DATA, requestData);
    }

    if (clientTraceId == null) {
      clientTraceId = request.getHeaders().get(Constant.Attr.CONTEXT_TRACE_ID);
    }

    String serverTraceId = clientTraceId != null ? clientTraceId : UUID.randomUUID().toString();

    Map<String, Object> map = new ConcurrentHashMap<>();
    map.put(Constant.Attr.CONTEXT_TRACE_ID, serverTraceId);
    if (clientTraceId != null) {
      map.put(Constant.Attr.CLIENT_TRACE_ID, clientTraceId);
    }
    if (clientTimestamp != null) {
      map.put(Constant.Attr.CLIENT_TIMESTAMP, clientTimestamp);
    }

    Context context = new Context(map);
    mutableContext.add(context);
  }

  @ResponseFilter
  public void filterResponse(final HttpRequest<?> request, final MutableHttpResponse<?> response) {
    boolean includeMetadata = config.getBoolean(Constant.Config.INCLUDE_METADATA, true);

    final OffsetDateTime serverRequestTimestamp = request
        .getAttribute(Constant.Attr.TIMESTAMP, OffsetDateTime.class)
        .orElse(OffsetDateTime.now());

    Map<String, Object> meta = null;
    if (includeMetadata) {
      meta = buildMeta(request, response, serverRequestTimestamp);
    }

    Object body = response.body();
    if (body instanceof Optional<?> opt) {
      body = opt.orElse(null);
    }

    if (body instanceof ResponseEnvelope existing) {
      // Already a ResponseEnvelope (from ApiExceptionHandler) — augment meta
      if (includeMetadata) {
        Map<String, Object> mergedMeta = new LinkedHashMap<>();
        if (existing.meta() != null) {
          mergedMeta.putAll(existing.meta());
        }
        mergedMeta.putAll(meta);
        response.body(new ResponseEnvelope(existing.data(), existing.errors(), mergedMeta));
      }
    } else {
      // Wrap raw body into the standard success envelope
      response.body(new ResponseEnvelope(body, null, meta));
    }
  }

  private Map<String, Object> buildMeta(final HttpRequest<?> request,
      final MutableHttpResponse<?> response,
      final OffsetDateTime serverRequestTimestamp) {

    Map<String, Object> meta = new LinkedHashMap<>();

    String clientTraceId = null;
    String clientTimestamp = null;
    try {
      clientTraceId = Context.get(Constant.Attr.CLIENT_TRACE_ID, (String) null);
      clientTimestamp = Context.get(Constant.Attr.CLIENT_TIMESTAMP, (String) null);
    } catch (IllegalStateException ignored) {
    }

    if (clientTimestamp != null) {
      meta.put(Constant.Meta.CLIENT_REQUEST_TIMESTAMP, clientTimestamp);
    }
    if (clientTraceId != null) {
      meta.put(Constant.Meta.CLIENT_TRACE_ID, clientTraceId);
    }

    meta.put(Constant.Meta.SERVER_API_URL, request.getUri().getPath());
    meta.put(Constant.Meta.SERVER_REQUEST_TIMESTAMP, serverRequestTimestamp.toString());

    String serverTraceId;
    try {
      serverTraceId = Context.get(Constant.Attr.CONTEXT_TRACE_ID, UUID.randomUUID().toString());
    } catch (IllegalStateException e) {
      serverTraceId = UUID.randomUUID().toString();
    }
    meta.put(Constant.Meta.SERVER_REQUEST_TRACE_ID, serverTraceId);

    OffsetDateTime now = OffsetDateTime.now();
    meta.put(Constant.Meta.SERVER_RESPONSE_TIMESTAMP, now.toString());
    meta.put(Constant.Meta.SERVER_RESPONSE_DURATION,
        Duration.between(serverRequestTimestamp, now).toString());

    HttpStatus status = response.status();
    meta.put(Constant.Meta.SERVER_RESPONSE_HTTP_STATUS,
        status.getCode() + " " + status.getReason());

    return meta;
  }
}
