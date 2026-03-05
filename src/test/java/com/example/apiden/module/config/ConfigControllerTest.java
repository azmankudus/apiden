package com.example.apiden.module.config;

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
 * Integration tests for ConfigController with the new path-based structure.
 */
@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConfigControllerTest {

  private static final Logger log = LoggerFactory.getLogger(ConfigControllerTest.class);

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  @Order(1)
  void testGetAllConfig() {
    log.info("Testing GET /config.");
    HttpRequest<?> request = HttpRequest.GET("/config");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());
    Map<String, Object> body = extractBody(response.body());
    assertTrue(body.containsKey("application.api.envelope.include-client-headers"));
  }

  @Test
  @Order(2)
  void testGetConfigByName() {
    log.info("Testing GET /config/application.api.envelope.include-client-headers.");
    HttpRequest<?> request = HttpRequest.GET("/config/application.api.envelope.include-client-headers");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());
    Map<String, Object> body = extractBody(response.body());
    assertEquals("application.api.envelope.include-client-headers", body.get("name"));
    assertNotNull(body.get("value"));
  }

  @Test
  @Order(3)
  void testUpdateConfig() {
    log.info("Testing PUT /config/{name}.");
    String prop = "application.api.envelope.include-client-headers";

    // Payload structure for @ApiBody
    Map<String, Object> payload = Map.of(
        "client", Map.of(
            "request", Map.of(
                "body", false)));

    HttpRequest<?> request = HttpRequest.PUT("/config/" + prop, payload);
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());
    Map<String, Object> body = extractBody(response.body());

    assertEquals(prop, body.get("name"));
    assertEquals(false, body.get("new_value"));
  }

  @Test
  @Order(4)
  void testConfigImpactOnFilter() {
    log.info("Verifying change impact on ApiFilter.");
    HttpRequest<?> request = HttpRequest.GET("/hello");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    Map<String, Object> body = response.body();
    Map<String, Object> clientBlock = (Map<String, Object>) body.get("client");
    Map<String, Object> clientHttp = (Map<String, Object>) clientBlock.get("http");

    // it should be suppressed because we set it to false
    if (clientHttp != null) {
      assertNull(clientHttp.get("headers"));
    }
  }

  @Test
  @Order(5)
  void testRestoreConfig() {
    log.info("Restoring config.");
    String prop = "application.api.envelope.include-client-headers";
    Map<String, Object> payload = Map.of(
        "client", Map.of(
            "request", Map.of(
                "body", true)));

    client.toBlocking().exchange(HttpRequest.PUT("/config/" + prop, payload),
        Argument.mapOf(String.class, Object.class));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> extractBody(Map<String, Object> envelope) {
    Map<String, Object> server = (Map<String, Object>) envelope.get("server");
    Map<String, Object> serverResponse = (Map<String, Object>) server.get("response");
    return (Map<String, Object>) serverResponse.get("body");
  }
}
