package com.example.apiden.shared.api;

import io.micronaut.core.annotation.Order;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.annotation.RequestFilter;
import io.micronaut.http.annotation.ResponseFilter;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.ServerFilter;
import io.micronaut.json.JsonMapper;
import io.micronaut.core.propagation.MutablePropagatedContext;
import io.micronaut.context.annotation.Value;
import io.micronaut.context.propagation.slf4j.MdcPropagationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Map;

/**
 * A highly prioritized server filter that manages API request/response envelopes.
 * It ensures traceability by generating or propagating trace IDs and captures
 * request/response metadata.
 */
@ServerFilter(Filter.MATCH_ALL_PATTERN)
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class ApiFilter {

  /** logger instance for ApiFilter. */
  private static final Logger log = LoggerFactory.getLogger(ApiFilter.class);

  public static final String ENVELOPE_ATTR = "api_envelope";
  public static final String TIMESTAMP_ATTR = "server_request_timestamp";
  public static final String SERVER_TRACE_ID_ATTR = "server_trace_id";
  public static final String CONTEXT_TRACE_ID = "traceid";

  private final JsonMapper jsonMapper;
  private final boolean includeClientHeaders;
  private final boolean includeServerHeaders;

  /**
   * Constructs the ApiFilter with necessary dependencies and configuration.
   *
   * @param jsonMapper The Micronaut JSON mapper.
   * @param includeClientHeaders Configuration flag to include client headers in the envelope.
   * @param includeServerHeaders Configuration flag to include server headers in the envelope.
   */
  ApiFilter(final JsonMapper jsonMapper,
      @Value("${application.api.envelope.include-client-headers:true}") final boolean includeClientHeaders,
      @Value("${application.api.envelope.include-server-headers:true}") final boolean includeServerHeaders) {
    log.debug("Initializing ApiFilter with includeClientHeaders={} and includeServerHeaders={}",
        includeClientHeaders, includeServerHeaders);
    this.jsonMapper = jsonMapper;
    this.includeClientHeaders = includeClientHeaders;
    this.includeServerHeaders = includeServerHeaders;
  }

  /**
   * Processes the incoming request, initializing the envelope and trace IDs.
   *
   * @param request The incoming HTTP request.
   * @param bodyBytes The raw bytes of the request body.
   * @param propagatedContext The context for MDC propagation.
   */
  @RequestFilter
  public void filterRequest(final HttpRequest<?> request, @Body final byte[] bodyBytes,
      final MutablePropagatedContext propagatedContext) {
    log.trace("Processing inbound request filter for: {} {}", request.getMethod(), request.getUri());
    request.setAttribute(TIMESTAMP_ATTR, OffsetDateTime.now());

    ApiObject apiObject = null;
    if (bodyBytes != null && bodyBytes.length > 0) {
      try {
        log.trace("Attempting to parse inbound ApiObject from body bytes (length: {}).", bodyBytes.length);
        apiObject = jsonMapper.readValue(bodyBytes, ApiObject.class);
      } catch (final IOException e) {
        log.error("Failed to parse ApiObject from request body: {}. Proceeding with default.", e.getMessage());
      }
    }

    if (apiObject == null) {
      log.debug("No ApiObject found in request; building default instance.");
      apiObject = ApiObjectBuilder.builder().build();
    }

    // Resolve or generate Server Trace ID
    String serverTraceId = null;
    if (apiObject.client() != null && apiObject.client().request() != null) {
      serverTraceId = apiObject.client().request().traceid();
    }

    if (!StringUtils.hasText(serverTraceId)) {
      serverTraceId = UUID.randomUUID().toString();
      log.debug("No traceid provided by client; generated new server traceid: {}", serverTraceId);
    } else {
      log.trace("Propagating client traceid as server traceid: {}", serverTraceId);
    }

    // Capture client headers if enabled
    if (includeClientHeaders) {
      ClientInfo clientInfo = apiObject.client();
      if (clientInfo == null) {
        clientInfo = ClientInfoBuilder.builder().build();
      }

      ClientHttpInfo clientHttp = clientInfo.http();
      if (clientHttp == null) {
        clientHttp = ClientHttpInfoBuilder.builder().build();
      }

      if (clientHttp.headers() == null) {
        log.trace("Capturing client headers for ApiObject envelope.");
        clientHttp = ClientHttpInfoBuilder.builder()
            .headers(request.getHeaders().asMap())
            .build();
        clientInfo = ClientInfoBuilder.builder()
            .http(clientHttp)
            .request(clientInfo.request())
            .response(clientInfo.response())
            .build();
        apiObject = ApiObjectBuilder.builder()
            .client(clientInfo)
            .server(apiObject.server())
            .build();
      }
    }

    request.setAttribute(ENVELOPE_ATTR, apiObject);
    request.setAttribute(SERVER_TRACE_ID_ATTR, serverTraceId);

    log.info("Inbound request processed: {} {} (TraceID: {})", request.getMethod(), request.getUri(), serverTraceId);

    // Propagate trace ID for MDC logging
    propagatedContext.add(new MdcPropagationContext(Map.of(CONTEXT_TRACE_ID, serverTraceId)));
  }

  /**
   * Processes the outgoing response, finalizing the envelope with server metadata.
   *
   * @param request The original HTTP request.
   * @param response The mutable HTTP response.
   */
  @ResponseFilter
  @SuppressWarnings("unchecked")
  public void filterResponse(final HttpRequest<?> request, final MutableHttpResponse<?> response) {
    log.trace("Processing outbound response filter for: {}", request.getUri());

    ApiObject apiObject = request.getAttribute(ENVELOPE_ATTR, ApiObject.class).orElse(null);
    final OffsetDateTime serverRequestTimestamp = request.getAttribute(TIMESTAMP_ATTR, OffsetDateTime.class)
        .orElse(OffsetDateTime.now());
    final String serverTraceId = request.getAttribute(SERVER_TRACE_ID_ATTR, String.class).orElse("unknown");

    if (apiObject == null) {
      log.warn("ApiObject attribute missing in response filter for TraceID: {}", serverTraceId);
      apiObject = ApiObjectBuilder.builder().build();
    }

    // Build Server Metadata
    final ServerHttpInfoBuilder shib = ServerHttpInfoBuilder.builder()
        .status(response.status().getCode());
    if (includeServerHeaders) {
      log.trace("Including server headers in response envelope.");
      shib.headers(response.getHeaders().asMap());
    }

    final ServerHttpInfo serverHttpInfo = shib.build();

    final ServerRequestInfo serverRequestInfo = ServerRequestInfoBuilder.builder()
        .timestamp(serverRequestTimestamp)
        .traceid(serverTraceId)
        .build();

    final OffsetDateTime serverResponseTimestamp = OffsetDateTime.now();
    final Duration duration = Duration.between(serverRequestTimestamp, serverResponseTimestamp);
    final String durationStr = duration.toString();

    final ServerResponseInfoBuilder srb = ServerResponseInfoBuilder.builder()
        .timestamp(serverResponseTimestamp)
        .duration(durationStr);

    final Object body = response.body();
    if (body instanceof ResponseBody responseBody) {
      log.debug("Formatting response body of type ResponseBody.");
      srb.status(responseBody.status().getValue())
          .code(responseBody.code())
          .message(responseBody.message())
          .body(responseBody.body());
    } else {
      log.debug("Formatting generic response body.");
      srb.status("success").code("0").message("Success").body(body);
    }

    final ServerInfo serverInfo = ServerInfoBuilder.builder()
        .http(serverHttpInfo)
        .request(serverRequestInfo)
        .response(srb.build())
        .build();

    final ApiObject finalApiObject = ApiObjectBuilder.builder()
        .client(apiObject.client())
        .server(serverInfo)
        .build();

    ((MutableHttpResponse<Object>) response).body(finalApiObject);

    log.info("Outbound response processed: {} {} (Status: {}, Duration: {}, TraceID: {})",
        request.getMethod(), request.getUri(), response.status().getCode(), durationStr, serverTraceId);
  }
}
