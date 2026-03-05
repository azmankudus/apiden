package com.example.apiden.module.hello;

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

/**
 * Unit tests for HelloController verifying both domain logic and the custom API mechanism (Envelope).
 */
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
        io.micronaut.core.type.Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());

    Map<String, Object> body = response.body();
    assertNotNull(body, "Response body should not be null");

    Map<String, Object> server = (Map<String, Object>) body.get("server");
    assertNotNull(server, "Expected 'server' block in the API envelope");

    Map<String, Object> serverResponse = (Map<String, Object>) server.get("response");
    assertNotNull(serverResponse, "Expected 'response' block inside 'server'");

    assertEquals("success", serverResponse.get("status"));
    assertEquals("0", serverResponse.get("code"));
    assertEquals("Success", serverResponse.get("message"));

    Map<String, Object> actualData = (Map<String, Object>) serverResponse.get("body");
    assertEquals("Hello World", actualData.get("message"));
  }

  @Test
  @SuppressWarnings("unchecked")
  void testPostHello() {
    // Wrap payload in client.request.body envelope so ApiBodyBinder works properly
    Map<String, Object> payload = Map.of(
        "client", Map.of(
            "request", Map.of(
                "body", Map.of("message", "Apiden"))));

    HttpRequest<?> request = HttpRequest.POST("/hello", payload);
    Map<String, Object> body;
    try {
      HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
          io.micronaut.core.type.Argument.mapOf(String.class, Object.class));
      assertEquals(200, response.status().getCode());
      body = response.body();
    } catch (HttpClientResponseException e) {
      System.out.println("ERROR RESPONSE: " + e.getResponse().getBody(String.class).orElse("empty"));
      throw e;
    }

    Map<String, Object> server = (Map<String, Object>) body.get("server");
    Map<String, Object> serverResponse = (Map<String, Object>) server.get("response");

    assertEquals("success", serverResponse.get("status"));

    Map<String, Object> actualData = (Map<String, Object>) serverResponse.get("body");
    assertEquals("nedipA", actualData.get("message")); // "Apiden" reversed
  }

  @Test
  @SuppressWarnings("unchecked")
  void testGetError() {
    HttpRequest<?> request = HttpRequest.GET("/hello/error");

    HttpClientResponseException thrown = assertThrows(HttpClientResponseException.class, () -> {
      client.toBlocking().exchange(request, io.micronaut.core.type.Argument.mapOf(String.class, Object.class));
    });

    assertEquals(500, thrown.getStatus().getCode());

    Map<String, Object> body = (Map<String, Object>) thrown.getResponse().body();
    assertNotNull(body, "Response body should not be null even on error");

    Map<String, Object> server = (Map<String, Object>) body.get("server");
    Map<String, Object> serverResponse = (Map<String, Object>) server.get("response");

    assertEquals("error", serverResponse.get("status"));
    assertEquals("ERR-100", serverResponse.get("code"));
  }
}
