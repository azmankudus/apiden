package com.example.apiden.module.log;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for LogController with the new path-based structure.
 */
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LogControllerTest {

  private static final Logger log = LoggerFactory.getLogger(LogControllerTest.class);

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  @Order(1)
  void testGetAllLoggers() {
    log.info("Testing GET /log.");
    HttpRequest<?> request = HttpRequest.GET("/log");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());
    Map<String, Object> body = extractBody(response.body());
    assertTrue(body.containsKey("root"));
    assertTrue(body.containsKey("com.example.apiden"));
  }

  @Test
  @Order(2)
  void testGetLoggerByName() {
    log.info("Testing GET /log/root.");
    HttpRequest<?> request = HttpRequest.GET("/log/root");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());
    Map<String, Object> body = extractBody(response.body());
    assertEquals("root", body.get("name"));
    assertNotNull(body.get("level"));
  }

  @Test
  @Order(3)
  void testUpdateLoggerLevel() {
    log.info("Testing PUT /log/{name}.");
    String logger = "com.example.apiden";

    Map<String, Object> payload = Map.of(
        "client", Map.of(
            "request", Map.of(
                "body", "DEBUG")));

    HttpRequest<?> request = HttpRequest.PUT("/log/" + logger, payload);
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());
    Map<String, Object> body = extractBody(response.body());

    assertEquals(logger, body.get("name"));
    assertEquals("DEBUG", body.get("new_level"));
  }

  @Test
  @Order(4)
  void testNonExistentLoggerImplicitCreate() {
    log.info("Testing PUT /log on a new deeper package.");
    String logger = "com.example.apiden.deeper.package";
    Map<String, Object> payload = Map.of(
        "client", Map.of(
            "request", Map.of(
                "body", "TRACE")));

    HttpRequest<?> request = HttpRequest.PUT("/log/" + logger, payload);
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());
    Map<String, Object> body = extractBody(response.body());
    assertEquals(logger, body.get("name"));
    assertEquals("TRACE", body.get("new_level"));
  }

  @Test
  @Order(5)
  void testRestoreLogLevel() {
    log.info("Restoring log level.");
    String logger = "com.example.apiden";
    Map<String, Object> payload = Map.of(
        "client", Map.of(
            "request", Map.of(
                "body", "INFO")));

    client.toBlocking().exchange(HttpRequest.PUT("/log/" + logger, payload),
        Argument.mapOf(String.class, Object.class));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> extractBody(Map<String, Object> envelope) {
    Map<String, Object> server = (Map<String, Object>) envelope.get("server");
    Map<String, Object> serverResponse = (Map<String, Object>) server.get("response");
    return (Map<String, Object>) serverResponse.get("body");
  }
}
