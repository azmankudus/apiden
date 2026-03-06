package com.example.apiden.feature.management;

import com.example.apiden.core.infra.Constant;
import com.example.apiden.core.infra.ConfigManager;
import com.example.apiden.core.infra.Message;
import com.example.apiden.core.web.ApiBody;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Put;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Consumes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

/**
 * Controller for managing runtime application configurations.
 */
@Tag(name = "Management", description = "System management and configuration")
@Controller("/config")
public final class ConfigController {

  private final ConfigManager configManager;
  private final Message messages;

  /**
   * Constructs a new ConfigController.
   *
   * @param configManager The manager for runtime configurations.
   * @param messages      The message source for localization.
   */
  ConfigController(final ConfigManager configManager, final Message messages) {
    this.configManager = configManager;
    this.messages = messages;
  }

  /**
   * Retrieves all live-updatable configurations.
   *
   * @return map of configurations
   */
  @Operation(summary = "List configuration", description = "Retrieves all current live-updatable configurations.")
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getAll() {
    return Map.of(
        messages.get(Constant.Message.Management.MSG_ALL_CONFIG),
        configManager.getAll());
  }

  /**
   * Retrieves a specific configuration by name.
   *
   * @param name property key
   * @return property value or error
   */
  @Operation(summary = "Get configuration", description = "Retrieves the value of a specific configuration property.")
  @Get("/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getByName(final String name) {
    Object value = configManager.get(name);

    if (value == null) {
      return Map.of(
          Constant.Code.NOT_FOUND,
          messages.get(Constant.Message.Management.ERR_PROP_NOT_FOUND));
    }

    return Map.of(
        messages.get(Constant.Message.Management.MSG_PROP_DETAILS),
        Map.of(Constant.Key.NAME, name, Constant.Key.VALUE, value));
  }

  /**
   * Updates a configuration property at runtime.
   *
   * @param name property key
   * @param body new value
   * @return update confirmation with old and new values
   * @throws IllegalAccessException if the update is not allowed
   */
  @Operation(summary = "Update configuration", description = "Updates a configuration property at runtime if allowed.")
  @Put("/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> update(final String name, @ApiBody final Object body)
      throws IllegalAccessException {
    Object oldValue = configManager.update(name, body);

    return Map.of(messages.get(Constant.Message.Management.MSG_PROP_UPDATED),
        Map.of(
            Constant.Key.NAME, name,
            Constant.Key.OLD_VALUE, oldValue != null ? oldValue : messages.get(Constant.Message.Core.TXT_NULL),
            Constant.Key.NEW_VALUE, body));
  }
}
