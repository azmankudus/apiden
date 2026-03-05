package com.example.apiden.shared.api;

import io.micronaut.core.annotation.Order;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
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
import io.micronaut.core.propagation.PropagatedContext;
import io.micronaut.context.propagation.slf4j.MdcPropagationContext;
import io.micronaut.context.annotation.Value;

import com.example.apiden.shared.infrastructure.ConfigManager;
import com.example.apiden.shared.infrastructure.Context;
import com.example.apiden.shared.infrastructure.Message;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Optional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;
import java.net.InetAddress;

/**
 * A highly prioritized server filter that manages API request/response envelopes.
 * Uses Micronaut's PropagatedContext for unified state management.
 */
@ServerFilter(Filter.MATCH_ALL_PATTERN)
@Order(Ordered.HIGHEST_PRECEDENCE)
public final class ApiFilter {

  private static final Logger log = LoggerFactory.getLogger(ApiFilter.class);

  private final JsonMapper jsonMapper;
  private final ConfigManager config;
  private final Message message;
  private final Map<String, Object> staticServerInfo;

  @Inject
  ApiFilter(final JsonMapper jsonMapper,
      final ConfigManager config,
      final Message message,
      @Value("${" + ApiConstants.Config.INSTANCE_NAME + ":apiden}") final String instanceName,
      @Value("${" + ApiConstants.Config.SERVER_HOST + ":}") final String serverHost) {

    this.jsonMapper = jsonMapper;
    this.config = config;
    this.message = message;

    Map<String, Object> info;
    try {
      info = precalculateServerInfo(instanceName, serverHost);
    } catch (Exception e) {
      info = new HashMap<>();
    }
    this.staticServerInfo = info;
  }

  private Map<String, Object> precalculateServerInfo(final String instanceName, final String serverHost) {
    final Map<String, Object> info = new HashMap<>();
    info.put(ApiConstants.Info.INSTANCE, instanceName);
    try {
      final String host = StringUtils.hasText(serverHost) ? serverHost : InetAddress.getLocalHost().getHostName();
      info.put(ApiConstants.Info.HOSTNAME, host);
    } catch (final Exception e) {
      info.put(ApiConstants.Info.HOSTNAME, "unknown");
    }
    info.put(ApiConstants.Info.RUNTIME, "Java " + System.getProperty("java.version") + " ("
        + System.getProperty("java.vendor") + ")");
    return info;
  }

  @RequestFilter
  public void filterRequest(final HttpRequest<?> request, final MutablePropagatedContext mutableContext,
      @Nullable @Body String body) {
    log.trace("ApiFilter.filterRequest for: {}", request.getUri());

    request.setAttribute(ApiConstants.Attr.TIMESTAMP, OffsetDateTime.now());

    ApiObject apiObject = null;
    if (body != null && !body.isEmpty()) {
      try {
        apiObject = jsonMapper.readValue(body, ApiObject.class);
      } catch (Exception e) {
        log.trace("Failed to parse body as ApiObject: {}", e.getMessage());
      }
    }

    if (apiObject == null) {
      apiObject = ApiObjectBuilder.builder().build();
    }

    String traceId = null;
    if (apiObject.client() != null && apiObject.client().request() != null) {
      traceId = apiObject.client().request().traceid();
    }
    if (traceId == null) {
      traceId = request.getHeaders().get(ApiConstants.Attr.CONTEXT_TRACE_ID);
    }
    if (traceId == null) {
      traceId = UUID.randomUUID().toString();
    }

    Locale locale = null;
    if (apiObject.client() != null && apiObject.client().request() != null) {
      String lang = apiObject.client().request().language();
      if (lang != null) {
        locale = Locale.of(lang);
      }
    }
    if (locale == null) {
      locale = request.getLocale().orElse(Locale.of("en"));
    }

    Map<String, Object> map = new ConcurrentHashMap<>();
    map.putAll(staticServerInfo);
    map.put(ApiConstants.Attr.CONTEXT_TRACE_ID, traceId);
    map.put(ApiConstants.Attr.CONTEXT_LANGUAGE, locale.toString());
    map.put(ApiConstants.Attr.ENVELOPE, apiObject);

    Context context = new Context(map);
    mutableContext.add(context);
  }

  @ResponseFilter
  public void filterResponse(final HttpRequest<?> request, final MutableHttpResponse<?> response) {
    final ApiObject apiObject = Context.get(ApiConstants.Attr.ENVELOPE, ApiObjectBuilder.builder().build());

    final String traceId = Context.get(ApiConstants.Attr.CONTEXT_TRACE_ID, "unknown");
    final Locale locale = Context.get(ApiConstants.Attr.CONTEXT_LANGUAGE, Locale.of("en"));

    final OffsetDateTime serverRequestTimestamp = request
        .getAttribute(ApiConstants.Attr.TIMESTAMP, OffsetDateTime.class)
        .orElse(OffsetDateTime.now());

    final ServerHttp serverHttp = ServerHttpBuilder.builder()
        .status(response.status().getCode())
        .headers(
            config.getBoolean(ApiConstants.Config.INCLUDE_SERVER_HEADERS, true) ? response.getHeaders().asMap() : null)
        .build();

    final ServerRequest serverRequest = ServerRequestBuilder.builder()
        .timestamp(serverRequestTimestamp)
        .traceid(traceId)
        .build();

    final ServerResponseBuilder srb = ServerResponseBuilder.builder()
        .timestamp(OffsetDateTime.now())
        .duration(Duration.between(serverRequestTimestamp, OffsetDateTime.now()).toString())
        .language(locale.toString());

    Object body = response.body();
    if (body instanceof Optional<?> opt) {
      body = opt.orElse(null);
    }

    if (body instanceof ResponseBody rb) {
      srb.status(rb.status().getValue()).code(rb.code()).message(rb.message()).body(rb.body());
    } else {
      srb.status(ResponseStatus.SUCCESS.getValue()).code(ApiConstants.Code.SUCCESS)
          .message(message.get(ApiConstants.Msg.SUCCESS)).body(body);
    }

    final Server server = ServerBuilder.builder()
        .http(serverHttp).request(serverRequest).response(srb.build())
        .info(config.getBoolean(ApiConstants.Config.INCLUDE_SERVER_INFO, true) ? staticServerInfo : null)
        .build();

    final ClientBuilder cb = ClientBuilder.builder();
    if (apiObject.client() != null) {
      ClientRequest origReq = apiObject.client().request();
      if (origReq != null) {
        cb.request(ClientRequestBuilder.builder()
            .traceid(origReq.traceid()).timestamp(origReq.timestamp()).language(origReq.language()).body(origReq.body())
            .build());
      }
      cb.http(apiObject.client().http());
    }
    if (config.getBoolean(ApiConstants.Config.INCLUDE_CLIENT_INFO, true)) {
      cb.info(extractClientInfo(request));
    }

    response.body(ApiObjectBuilder.builder().client(cb.build()).server(server).build());
  }

  private Map<String, Object> extractClientInfo(final HttpRequest<?> request) {
    final Map<String, Object> clientInfo = new HashMap<>();
    String ua = request.getHeaders().get("User-Agent");
    if (ua != null)
      clientInfo.put(ApiConstants.Info.BROWSER, ua);

    try {
      clientInfo.put(ApiConstants.Info.IP, request.getRemoteAddress().getAddress().getHostAddress());
    } catch (Exception ignored) {
    }

    String fwd = request.getHeaders().get("X-Forwarded-For");
    if (fwd != null)
      clientInfo.put(ApiConstants.Info.FORWARDED_IP, fwd);

    return clientInfo;
  }
}
