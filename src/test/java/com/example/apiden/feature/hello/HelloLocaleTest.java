package com.example.apiden.feature.hello;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class HelloLocaleTest {

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  @SuppressWarnings("unchecked")
  void testGetHelloEnglish() {
    HttpRequest<?> request = HttpRequest.GET("/api/hello").header("Accept-Language", "en");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());

    Map<String, Object> body = response.body();
    Map<String, Object> data = (Map<String, Object>) body.get("data");
    assertNotNull(data);
    assertEquals("Hello World", data.get("message"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetHelloDefaultIfMissing() {
    // No language header, should fallback to default (en)
    HttpRequest<?> request = HttpRequest.GET("/api/hello");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());

    Map<String, Object> body = response.body();
    Map<String, Object> data = (Map<String, Object>) body.get("data");
    assertNotNull(data);
    assertEquals("Hello World", data.get("message"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testPostHelloWithDataEnvelope() {
    // POST with data envelope
    Map<String, Object> payload = Map.of(
        "data", Map.of("message", "Apiden"));

    HttpRequest<?> request = HttpRequest.POST("/api/hello", payload);
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());

    Map<String, Object> body = response.body();
    Map<String, Object> data = (Map<String, Object>) body.get("data");
    assertNotNull(data);
    assertEquals("nedipA", data.get("message")); // "Apiden" reversed
  }

  @Test
  @SuppressWarnings("unchecked")
  void testPostHelloWithMetaTraceId() {
    // POST with client trace ID in meta
    Map<String, Object> payload = Map.of(
        "data", Map.of("message", "Apiden"),
        "meta", Map.of("client_trace_id", "test-trace-123"));

    HttpRequest<?> request = HttpRequest.POST("/api/hello", payload);
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());

    Map<String, Object> body = response.body();
    Map<String, Object> meta = (Map<String, Object>) body.get("meta");
    assertNotNull(meta);
    // The client trace ID should be echoed back in the meta
    assertEquals("test-trace-123", meta.get("client_trace_id"));
    assertEquals("test-trace-123", meta.get("server_request_trace_id"));
  }
}
