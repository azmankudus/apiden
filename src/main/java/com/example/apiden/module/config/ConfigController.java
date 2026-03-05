package com.example.apiden.module.config;

import com.example.apiden.shared.api.ApiBody;
import com.example.apiden.shared.api.ApiConstants;
import com.example.apiden.shared.api.ResponseBody;
import com.example.apiden.shared.api.ResponseStatus;
import com.example.apiden.shared.infrastructure.ConfigManager;
import com.example.apiden.shared.infrastructure.Message;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Consumes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Controller for managing runtime configuration properties.
 */
@Controller("/config")
public final class ConfigController {

  private static final Logger log = LoggerFactory.getLogger(ConfigController.class);

  private final ConfigManager configManager;
  private final Message messages;

  @Inject
  ConfigController(final ConfigManager configManager, final Message messages) {
    this.configManager = configManager;
    this.messages = messages;
  }

  /**
   * Endpoint for retrieving all authorized configuration properties.
   *
   * @return A success response containing all authorized config names and values.
   */
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<ResponseBody> getAll() {
    log.info("Received request for GET /config");
    return HttpResponse.ok(new ResponseBody(
        ResponseStatus.SUCCESS,
        ApiConstants.Code.SUCCESS,
        messages.get(ApiConstants.Msg.ALL_CONFIGURATION),
        configManager.getAll()));
  }

  /**
   * Endpoint for retrieving a single configuration property by name.
   *
   * @param name   The name of the configuration property.
   * @return The property name and value in a success response, or Error if not found.
   */
  @Get("/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<ResponseBody> getByName(final String name) {
    log.info("Received request for GET /config/{}", name);
    Object value = configManager.get(name);

    if (value == null) {
      log.warn("Configuration property not found: {}", name);
      return HttpResponse.notFound(new ResponseBody(
          ResponseStatus.ERROR,
          ApiConstants.Code.NOT_FOUND,
          messages.get(ApiConstants.Msg.PROPERTY_NOT_FOUND),
          null));
    }

    return HttpResponse.ok(new ResponseBody(
        ResponseStatus.SUCCESS,
        ApiConstants.Code.SUCCESS,
        messages.get(ApiConstants.Msg.PROPERTY_DETAILS),
        Map.of(ApiConstants.Key.NAME, name, ApiConstants.Key.VALUE, value)));
  }

  /**
   * Endpoint for updating or creating a configuration property.
   *
   * @param name   The name of the configuration property.
   * @param body   The new value (wrapped in @ApiBody).
   * @return The property name, old value, and new value in a success response.
   * @throws IllegalAccessException if the property is not authorized for update.
   */
  @Put("/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<ResponseBody> update(final String name, @ApiBody final Object body)
      throws IllegalAccessException {
    log.info("Received request for PUT /config/{} with new value", name);

    // ConfigManager.update will throw IllegalAccessException if forbidden
    Object oldValue = configManager.update(name, body);

    return HttpResponse.ok(new ResponseBody(
        ResponseStatus.SUCCESS,
        ApiConstants.Code.SUCCESS,
        messages.get(ApiConstants.Msg.PROPERTY_UPDATED),
        Map.of(
            ApiConstants.Key.NAME, name,
            ApiConstants.Key.OLD_VALUE, oldValue != null ? oldValue : messages.get(ApiConstants.Label.NULL),
            ApiConstants.Key.NEW_VALUE, body)));
  }
}
