package com.example.apiden.core.web;

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

import com.example.apiden.core.infra.Constant;
import com.example.apiden.core.infra.ConfigManager;
import com.example.apiden.core.infra.Context;
import com.example.apiden.core.infra.ServerInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global filter for handling API requests and responses.
 * 
 * <p>This filter captures metadata from incoming requests (like client trace IDs),
 * manages the request context, and wraps outgoing responses in a standard API envelope.</p>
 */
@ServerFilter(Filter.MATCH_ALL_PATTERN)
@Order(Ordered.HIGHEST_PRECEDENCE)
final class CustomFilter {

  private static final Logger logger = LoggerFactory.getLogger(CustomFilter.class);

  private final JsonMapper jsonMapper;
  private final ConfigManager config;
  private final ServerInfo serverInfo;

  /**
   * Initializes the filter with required dependencies.
   *
   * @param jsonMapper for JSON parsing and serialization
   * @param config for accessing application settings
   * @param serverInfo for static server metadata
   */
  CustomFilter(final JsonMapper jsonMapper, final ConfigManager config, final ServerInfo serverInfo) {
    this.jsonMapper = jsonMapper;
    this.config = config;
    this.serverInfo = serverInfo;
  }

  /**
   * Processes incoming requests to establish context and extract metadata.
   *
   * @param request the incoming HTTP request
   * @param mutableContext context for propagating attributes
   * @param body the raw request body (optional)
   */
  @RequestFilter
  public void filterRequest(final HttpRequest<?> request, final MutablePropagatedContext mutableContext,
      @Nullable @Body String body) {

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

    if (clientTraceId == null) {
      clientTraceId = request.getHeaders().get(Constant.Attr.CONTEXT_TRACE_ID);
    }

    String serverTraceId = clientTraceId != null ? clientTraceId : UUID.randomUUID().toString();

    // Add Trace ID to MDC for logging
    MDC.put(Constant.Attr.CONTEXT_TRACE_ID, serverTraceId);

    // Store everything in the propagated context
    Map<String, Object> map = new ConcurrentHashMap<>();
    map.put(Constant.Attr.TIMESTAMP, OffsetDateTime.now());
    map.put(Constant.Attr.CONTEXT_TRACE_ID, serverTraceId);
    if (requestData != null) {
      map.put(Constant.Attr.REQUEST_DATA, requestData);
    }
    if (clientTraceId != null) {
      map.put(Constant.Attr.CLIENT_TRACE_ID, clientTraceId);
    }
    if (clientTimestamp != null) {
      map.put(Constant.Attr.CLIENT_TIMESTAMP, clientTimestamp);
    }

    // Extract Environment Info
    String userAgent = request.getHeaders().get("User-Agent");
    String clientIp = request.getRemoteAddress().getAddress().getHostAddress();
    String xForwardedFor = request.getHeaders().get("X-Forwarded-For");
    String clientOs = parseOs(userAgent);

    map.put(Constant.Attr.CLIENT_AGENT, userAgent != null ? userAgent : "Unknown");
    map.put(Constant.Attr.CLIENT_OS, clientOs);
    map.put(Constant.Attr.CLIENT_IP, clientIp != null ? clientIp : "Unknown");
    map.put(Constant.Attr.CLIENT_ACTUAL_IP,
        xForwardedFor != null ? xForwardedFor : (clientIp != null ? clientIp : "Unknown"));

    Context.init(map);
    mutableContext.add(new Context(Context.getMap()));

    logger.debug("Incoming request: method={}, path={}, traceId={}",
        request.getMethod(), request.getPath(), serverTraceId);
  }

  /**
   * Parses the User-Agent string to identify the client's operating system.
   *
   * @param userAgent The User-Agent header value.
   * @return A descriptive OS string.
   */
  private String parseOs(final String userAgent) {
    if (userAgent == null)
      return "Unknown";
    if (userAgent.contains("Windows"))
      return "Windows";
    if (userAgent.contains("Mac"))
      return "Mac OS";
    if (userAgent.contains("Linux"))
      return "Linux";
    if (userAgent.contains("Android"))
      return "Android";
    if (userAgent.contains("iPhone") || userAgent.contains("iPad"))
      return "iOS";
    return "Unknown";
  }

  /**
   * Processes outgoing responses to wrap them in the standard envelope and augment with metadata.
   *
   * @param request the original HTTP request
   * @param response the mutable HTTP response to modify
   */
  @ResponseFilter
  public void filterResponse(final HttpRequest<?> request, final MutableHttpResponse<?> response) {
    try {
      boolean includeMetadata = config.getBoolean(Constant.Config.INCLUDE_METADATA, true);

      OffsetDateTime serverRequestTimestamp;
      try {
        serverRequestTimestamp = Context.get(Constant.Attr.TIMESTAMP, OffsetDateTime.now());
      } catch (IllegalStateException e) {
        serverRequestTimestamp = OffsetDateTime.now();
      }

      Map<String, Object> meta = null;
      if (includeMetadata) {
        meta = buildMeta(request, response, serverRequestTimestamp);
      }

      Object body = response.body();
      if (body instanceof Optional<?> opt) {
        body = opt.orElse(null);
      }

      if (body instanceof ResponseEnvelope existing) {
        if (includeMetadata) {
          Map<String, Object> mergedMeta = new LinkedHashMap<>();
          if (existing.meta() != null) {
            mergedMeta.putAll(existing.meta());
          }
          mergedMeta.putAll(meta);
          response.body(new ResponseEnvelope(existing.data(), existing.errors(), mergedMeta));
        }
      } else if (response.status().getCode() >= 400) {
        String code = String.format("00000%03d", response.status().getCode());
        String message = response.status().getReason();

        if (message == null || message.isBlank()) {
          message = "Error " + code;
        }

        ResponseError error = new ResponseError(code, message, null);
        response.body(new ResponseEnvelope(null, List.of(error), meta));
      } else {
        response.body(new ResponseEnvelope(body, null, meta));
      }

      logger.debug("Outgoing response: status={}, path={}", response.status().getCode(), request.getPath());

    } finally {
      MDC.remove(Constant.Attr.CONTEXT_TRACE_ID);
      Context.destroy();
    }
  }

  /**
   * Builds the metadata object for the API response.
   *
   * @param request                The current HTTP request.
   * @param response               The current HTTP response.
   * @param serverRequestTimestamp The timestamp when the server started processing the request.
   * @return A map containing response metadata.
   */
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

    meta.put(Constant.Meta.SERVER_URL_PATH, request.getUri().getPath());
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

    if (config.getBoolean(Constant.Config.INCLUDE_ENVIRONMENT_INFO, false)) {
      try {
        meta.put(Constant.Meta.CLIENT_AGENT, Context.get(Constant.Attr.CLIENT_AGENT, "Unknown"));
        meta.put(Constant.Meta.CLIENT_OS, Context.get(Constant.Attr.CLIENT_OS, "Unknown"));
        meta.put(Constant.Meta.CLIENT_IP, Context.get(Constant.Attr.CLIENT_IP, "Unknown"));
        meta.put(Constant.Meta.CLIENT_ACTUAL_IP, Context.get(Constant.Attr.CLIENT_ACTUAL_IP, "Unknown"));
      } catch (IllegalStateException ignored) {
      }
      meta.put(Constant.Meta.SERVER_INSTANCE_NAME, serverInfo.getInstanceName());
      meta.put(Constant.Meta.SERVER_OS, serverInfo.getOs());
      meta.put(Constant.Meta.SERVER_RUNTIME, serverInfo.getRuntime());
      meta.put(Constant.Meta.SERVER_IP, serverInfo.getIp());
    }

    return meta;
  }
}
