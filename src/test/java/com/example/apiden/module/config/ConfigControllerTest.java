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

@MicronautTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConfigControllerTest {

  private static final Logger logger = LoggerFactory.getLogger(ConfigControllerTest.class);

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  @Order(1)
  @SuppressWarnings("unchecked")
  void testGetAllConfig() {
    logger.info("Testing GET /config.");
    HttpRequest<?> request = HttpRequest.GET("/config");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());
    Map<String, Object> body = response.body();
    Map<String, Object> data = (Map<String, Object>) body.get("data");
    assertNotNull(data, "Expected 'data' in response envelope");
  }

  @Test
  @Order(2)
  @SuppressWarnings("unchecked")
  void testGetConfigByName() {
    logger.info("Testing GET /config/application.api.response.include-metadata.");
    HttpRequest<?> request = HttpRequest.GET("/config/application.api.response.include-metadata");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());
    Map<String, Object> body = response.body();
    Map<String, Object> data = (Map<String, Object>) body.get("data");
    assertNotNull(data);
  }

  @Test
  @Order(3)
  @SuppressWarnings("unchecked")
  void testUpdateConfig() {
    logger.info("Testing PUT /config/{name}.");
    String prop = "application.api.response.include-metadata";

    // Payload wrapped in standard {"data": ...} envelope
    Map<String, Object> payload = Map.of("data", false);

    HttpRequest<?> request = HttpRequest.PUT("/config/" + prop, payload);
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());
    Map<String, Object> body = response.body();
    Map<String, Object> data = (Map<String, Object>) body.get("data");
    assertNotNull(data);
  }

  @Test
  @Order(4)
  @SuppressWarnings("unchecked")
  void testConfigImpactOnFilter() {
    logger.info("Verifying change impact on ApiFilter.");
    HttpRequest<?> request = HttpRequest.GET("/hello");
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    Map<String, Object> body = response.body();

    // When include-metadata is false, meta should be null
    assertNull(body.get("meta"), "Expected 'meta' to be absent when include-metadata is disabled");
  }

  @Test
  @Order(5)
  void testRestoreConfig() {
    logger.info("Restoring config.");
    String prop = "application.api.response.include-metadata";
    Map<String, Object> payload = Map.of("data", true);

    client.toBlocking().exchange(HttpRequest.PUT("/config/" + prop, payload),
        Argument.mapOf(String.class, Object.class));
  }
}
