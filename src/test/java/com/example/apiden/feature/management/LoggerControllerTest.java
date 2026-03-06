package com.example.apiden.feature.management;

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
public class LoggerControllerTest {

  private static final Logger logger = LoggerFactory.getLogger(LoggerControllerTest.class);

  @Inject
  @Client("/")
  HttpClient client;

  @Test
  @Order(1)
  @SuppressWarnings("unchecked")
  void testGetAllLoggers() {
    logger.info("Testing GET /logger.");
    HttpRequest<?> request = HttpRequest.GET("/api/logger");
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
  void testGetLoggerByName() {
    logger.info("Testing GET /logger/root.");
    HttpRequest<?> request = HttpRequest.GET("/api/logger/root");
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
  void testUpdateLoggerLevel() {
    logger.info("Testing PUT /logger/{name}.");
    String loggerName = "com.example.apiden";

    // Payload wrapped in standard {"data": ...} envelope
    Map<String, Object> payload = Map.of("data", "DEBUG");

    HttpRequest<?> request = HttpRequest.PUT("/api/logger/" + loggerName, payload);
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
  void testNonExistentLoggerImplicitCreate() {
    logger.info("Testing PUT /logger on a new deeper package.");
    String loggerName = "com.example.apiden.deeper.package";
    Map<String, Object> payload = Map.of("data", "TRACE");

    HttpRequest<?> request = HttpRequest.PUT("/api/logger/" + loggerName, payload);
    HttpResponse<Map<String, Object>> response = client.toBlocking().exchange(request,
        Argument.mapOf(String.class, Object.class));

    assertEquals(200, response.status().getCode());
    Map<String, Object> body = response.body();
    Map<String, Object> data = (Map<String, Object>) body.get("data");
    assertNotNull(data);
  }

  @Test
  @Order(5)
  void testRestoreLogLevel() {
    logger.info("Restoring log level.");
    String loggerName = "com.example.apiden";
    Map<String, Object> payload = Map.of("data", "INFO");

    client.toBlocking().exchange(HttpRequest.PUT("/api/logger/" + loggerName, payload),
        Argument.mapOf(String.class, Object.class));
  }
}
