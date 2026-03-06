package com.example.apiden.module.config;

import com.example.apiden.shared.api.ApiBody;
import com.example.apiden.shared.infrastructure.Constant;
import com.example.apiden.shared.infrastructure.ConfigManager;
import com.example.apiden.shared.infrastructure.Message;

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

@Controller("/config")
public final class ConfigController {

  private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);

  private final ConfigManager configManager;
  private final Message messages;

  @Inject
  ConfigController(final ConfigManager configManager, final Message messages) {
    this.configManager = configManager;
    this.messages = messages;
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getAll() {
    return Map.of(
        messages.get(Constant.Msg.ALL_CONFIGURATION),
        configManager.getAll());
  }

  @Get("/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getByName(final String name) {
    Object value = configManager.get(name);

    if (value == null) {
      return Map.of(
          Constant.Code.NOT_FOUND,
          messages.get(Constant.Msg.PROPERTY_NOT_FOUND));
    }

    return Map.of(

        messages.get(Constant.Msg.PROPERTY_DETAILS),
        Map.of(Constant.Key.NAME, name, Constant.Key.VALUE, value));
  }

  @Put("/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> update(final String name, @ApiBody final Object body)
      throws IllegalAccessException {
    Object oldValue = configManager.update(name, body);

    return Map.of(messages.get(Constant.Msg.PROPERTY_UPDATED),
        Map.of(
            Constant.Key.NAME, name,
            Constant.Key.OLD_VALUE, oldValue != null ? oldValue : messages.get(Constant.Label.NULL),
            Constant.Key.NEW_VALUE, body));
  }
}
