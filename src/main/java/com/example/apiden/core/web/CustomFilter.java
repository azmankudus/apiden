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
import io.micronaut.core.propagation.MutablePropagatedContext;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;

import com.example.apiden.core.infra.Constant;
import com.example.apiden.core.infra.ConfigManager;
import com.example.apiden.core.infra.Context;
import com.example.apiden.core.infra.ServerInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
  public void filterRequest(final HttpRequest<?> request, 
      final MutablePropagatedContext mutableContext,
      @Nullable @Body String body) {

    Context.init(mutableContext);
    Context.set(Constant.Attr.TIMESTAMP, OffsetDateTime.now());

    // 1. Metadata from body
    extractMetadataFromBody(body);

    // 2. Trace ID logic
    updateTraceContext(request);

    // 3. Environment info
    populateEnvironmentInfo(request);

    if (logger.isDebugEnabled()) {
      logger.debug("Incoming request: method={}, path={}, traceId={}",
          request.getMethod(), request.getPath(), Context.get(Constant.Attr.CONTEXT_TRACE_ID, Constant.Value.UNKNOWN));
    }
  }

  /**
   * Parses the User-Agent string to identify the client's operating system.
   *
   * @param userAgent The User-Agent header value.
   * @return A descriptive OS string.
   */
  private String parseOs(final String userAgent) {
    if (userAgent == null)
      return Constant.Value.UNKNOWN;
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
    return Constant.Value.UNKNOWN;
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

      OffsetDateTime requestTimestamp;
      try {
        requestTimestamp = Context.get(Constant.Attr.TIMESTAMP, OffsetDateTime.now());
      } catch (IllegalStateException e) {
        requestTimestamp = OffsetDateTime.now();
      }

      Map<String, Object> meta = includeMetadata ? buildMeta(request, response, requestTimestamp) : null;
      wrapResponse(response, meta, includeMetadata);

      logger.debug("Outgoing response: status={}, path={}", response.status().getCode(), request.getPath());

    } finally {
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
      // Ignored: context may not be initialized if request filtering failed or was skipped
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
        meta.put(Constant.Meta.CLIENT_USER_AGENT, Context.get(Constant.Attr.CLIENT_USER_AGENT, Constant.Value.UNKNOWN));
        meta.put(Constant.Meta.CLIENT_OS, Context.get(Constant.Attr.CLIENT_OS, Constant.Value.UNKNOWN));
        meta.put(Constant.Meta.CLIENT_IP, Context.get(Constant.Attr.CLIENT_IP, Constant.Value.UNKNOWN));
        meta.put(Constant.Meta.CLIENT_ACTUAL_IP, Context.get(Constant.Attr.CLIENT_ACTUAL_IP, Constant.Value.UNKNOWN));
      } catch (IllegalStateException ignored) {
        // Ignored: context may not be initialized if request filtering failed or was skipped
      }
      meta.put(Constant.Meta.SERVER_INSTANCE_NAME, serverInfo.getInstanceName());
      meta.put(Constant.Meta.SERVER_OS, serverInfo.getOs());
      meta.put(Constant.Meta.SERVER_RUNTIME, serverInfo.getRuntime());
      meta.put(Constant.Meta.SERVER_IP, serverInfo.getIp());
    }

    return meta;
  }

  private void extractMetadataFromBody(final String body) {
    if (body == null || body.isEmpty()) {
      return;
    }

    try {
      JsonNode root = jsonMapper.readValue(body, JsonNode.class);
      if (!root.isObject()) {
        return;
      }

      Object data = extractJsonValue(root, Constant.Envelope.DATA);
      if (data != null) {
        Context.set(Constant.Attr.REQUEST_DATA, data);
      }

      JsonNode metaNode = root.get(Constant.Envelope.META);
      if (metaNode != null && metaNode.isObject()) {
        setIfAbsent(Constant.Attr.CLIENT_TRACE_ID, getString(metaNode, Constant.Meta.CLIENT_TRACE_ID));
        setIfAbsent(Constant.Attr.CLIENT_TIMESTAMP, getString(metaNode, Constant.Meta.CLIENT_REQUEST_TIMESTAMP));
      }
    } catch (Exception e) {
      logger.trace("Failed to parse body as API envelope: {}", e.getMessage());
    }
  }

  private void setIfAbsent(final String key, final Object value) {
    if (value != null && Context.get(key, null) == null) {
      Context.set(key, value);
    }
  }

  private Object extractJsonValue(JsonNode root, String key) throws java.io.IOException {
    JsonNode node = root.get(key);
    return node != null ? jsonMapper.readValue(jsonMapper.writeValueAsBytes(node), Object.class) : null;
  }

  private String getString(JsonNode node, String key) {
    JsonNode value = node.get(key);
    return (value != null && value.isString()) ? value.getStringValue() : null;
  }

  private void wrapResponse(MutableHttpResponse<?> response, Map<String, Object> meta, boolean includeMetadata) {
    Object body = response.body();
    if (body instanceof Optional<?> opt)
      body = opt.orElse(null);

    if (body instanceof ResponseEnvelope existing) {
      response.body(includeMetadata ? mergeResponseMeta(existing, meta) : existing);
    } else if (response.status().getCode() >= 400) {
      response.body(createErrorEnvelope(response, meta));
    } else {
      response.body(new ResponseEnvelope(body, null, meta));
    }
  }

  private ResponseEnvelope mergeResponseMeta(ResponseEnvelope existing, Map<String, Object> meta) {
    Map<String, Object> merged = new LinkedHashMap<>();
    if (existing.meta() != null)
      merged.putAll(existing.meta());
    if (meta != null)
      merged.putAll(meta);
    return new ResponseEnvelope(existing.data(), existing.errors(), merged);
  }

  private ResponseEnvelope createErrorEnvelope(MutableHttpResponse<?> response, Map<String, Object> meta) {
    String code = String.format("00000%03d", response.status().getCode());
    String msg = response.status().getReason();
    if (msg == null || msg.isBlank())
      msg = "Error " + code;
    return new ResponseEnvelope(null, List.of(new ResponseError(code, msg, null)), meta);
  }

  /**
   * Determines and updates the trace ID for the request.
   *
   * @param request The current request
   */
  private void updateTraceContext(final HttpRequest<?> request) {
    String clientTraceId = Context.get(Constant.Attr.CLIENT_TRACE_ID, null);
    if (clientTraceId == null) {
      clientTraceId = request.getHeaders().get(Constant.Attr.CONTEXT_TRACE_ID);
    }

    String serverTraceId = clientTraceId != null ? clientTraceId : UUID.randomUUID().toString();
    Context.set(Constant.Attr.CONTEXT_TRACE_ID, serverTraceId);
  }

  /**
   * Populates environmental client information (IP, OS).
   *
   * @param request The current request
   */
  private void populateEnvironmentInfo(final HttpRequest<?> request) {
    String userAgent = request.getHeaders().get("User-Agent");
    String clientIp = request.getRemoteAddress().getAddress().getHostAddress();
    String xForwardedFor = request.getHeaders().get("X-Forwarded-For");

    String displayClientIp = clientIp != null ? clientIp : Constant.Value.UNKNOWN;
    String actualIp = xForwardedFor != null ? xForwardedFor : displayClientIp;

    Context.set(Constant.Attr.CLIENT_USER_AGENT, userAgent != null ? userAgent : Constant.Value.UNKNOWN);
    Context.set(Constant.Attr.CLIENT_OS, parseOs(userAgent));
    Context.set(Constant.Attr.CLIENT_IP, displayClientIp);
    Context.set(Constant.Attr.CLIENT_ACTUAL_IP, actualIp);
  }
}
