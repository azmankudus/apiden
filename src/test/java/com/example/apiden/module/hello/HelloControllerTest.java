package com.example.apiden.module.hello;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class HelloControllerTest {

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  @SuppressWarnings("unchecked")
  void testGetHello() {
    HttpRequest<?> request = HttpRequest.GET("/hello");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());

    Map<String, Object> body = response.body();
    assertNotNull(body, "Response body should not be null");

    // New envelope: {"data": {...}, "meta": {...}}
    Map<String, Object> data = (Map<String, Object>) body.get("data");
    assertNotNull(data, "Expected 'data' block in the response envelope");
    assertEquals("Hello World", data.get("message"));

    // Meta should be present when include-metadata is true
    assertNotNull(body.get("meta"), "Expected 'meta' block in the response envelope");
  }

  @Test
  @SuppressWarnings("unchecked")
  void testPostHello() {
    // Wrap payload in {"data": ...} envelope so ApiBodyBinder works properly
    Map<String, Object> payload = Map.of(
        "data", Map.of("message", "Apiden"));

    HttpRequest<?> request = HttpRequest.POST("/hello", payload);
    Map<String, Object> body;
    try {
      HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
          Argument.mapOf(String.class, Object.class));
      assertEquals(200, response.status().getCode());
      body = response.body();
    } catch (HttpClientResponseException e) {
      System.out.println("ERROR RESPONSE: " + e.getResponse().getBody(String.class).orElse("empty"));
      throw e;
    }

    Map<String, Object> data = (Map<String, Object>) body.get("data");
    assertNotNull(data);
    assertEquals("nedipA", data.get("message")); // "Apiden" reversed
  }
}
