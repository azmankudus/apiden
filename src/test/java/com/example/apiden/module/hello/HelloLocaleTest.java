package com.example.apiden.module.hello;

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

/**
 * Test for verifying locale resolution from headers and body payload.
 */
@MicronautTest
public class HelloLocaleTest {

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  @SuppressWarnings("unchecked")
  void testGetHelloMalay() {
    // Request with Malay language header
    HttpRequest<?> request = HttpRequest.GET("/hello").header("Accept-Language", "ms");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());

    Map<String, Object> body = response.body();
    Map<String, Object> server = (Map<String, Object>) body.get("server");
    Map<String, Object> serverResponse = (Map<String, Object>) server.get("response");

    // Check language field in response
    assertEquals("ms", serverResponse.get("language"));

    // "Success" in Malay is "Berjaya"
    assertEquals("Berjaya", serverResponse.get("message"));

    Map<String, Object> actualData = (Map<String, Object>) serverResponse.get("body");
    // "Hello World" in Malay is "Hai Dunia"
    assertEquals("Hai Dunia", actualData.get("message"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetHelloEnglish() {
    // Request with English language header
    HttpRequest<?> request = HttpRequest.GET("/hello").header("Accept-Language", "en");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());

    Map<String, Object> body = response.body();
    Map<String, Object> server = (Map<String, Object>) body.get("server");
    Map<String, Object> serverResponse = (Map<String, Object>) server.get("response");

    assertEquals("en", serverResponse.get("language"));
    assertEquals("Success", serverResponse.get("message"));

    Map<String, Object> actualData = (Map<String, Object>) serverResponse.get("body");
    assertEquals("Hello World", actualData.get("message"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetHelloDefaultIfMissing() {
    // No header, should fallback to default (en)
    HttpRequest<?> request = HttpRequest.GET("/hello");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());

    Map<String, Object> body = response.body();
    Map<String, Object> server = (Map<String, Object>) body.get("server");
    Map<String, Object> serverResponse = (Map<String, Object>) server.get("response");

    assertEquals("en", serverResponse.get("language"));
    assertEquals("Success", serverResponse.get("message"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testPostHelloPayloadLanguage() {
    // Request with Malay language in payload
    Map<String, Object> payload = Map.of(
        "client", Map.of(
            "request", Map.of(
                "language", "ms",
                "body", Map.of("message", "Apiden"))));

    HttpRequest<?> request = HttpRequest.POST("/hello", payload);
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());

    Map<String, Object> body = response.body();
    Map<String, Object> server = (Map<String, Object>) body.get("server");
    Map<String, Object> serverResponse = (Map<String, Object>) server.get("response");

    // Should resolve to Malay "Berjaya"
    assertEquals("ms", serverResponse.get("language"));
    assertEquals("Berjaya", serverResponse.get("message"));
  }
}
