package com.example.apiden.core.web;

import com.example.apiden.core.infra.Constant;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import org.reactivestreams.Publisher;

/**
 * Filter to automatically add a standard User-Agent header to all client requests
 * if one is not already present. The User-Agent is injected via configuration.
 */
@Filter("/**")
public class UserAgentClientFilter implements HttpClientFilter {

  private final String userAgent;

  /**
   * Initializes the filter with the configured User-Agent string.
   *
   * @param clientAgent The library agent string injected from properties.
   */
  public UserAgentClientFilter(@Value("${" + Constant.Config.CLIENT_USER_AGENT + "}") String clientAgent) {
    String os = System.getProperty("os.name");
    String arch = System.getProperty("os.arch");
    String java = System.getProperty("java.version");
    this.userAgent = String.format("Mozilla/5.0 (%s %s) Java/%s %s", os, arch, java, clientAgent);
  }

  @Override
  public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
    if (!request.getHeaders().contains("User-Agent")) {
      request.header("User-Agent", userAgent);
    }
    return chain.proceed(request);
  }
}
