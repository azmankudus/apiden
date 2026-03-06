package com.example.apiden.feature.management;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

import com.example.apiden.core.infra.Constant;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing runtime logging levels.
 */
@Tag(name = "Management", description = "System management and configuration")
@Controller("/logger")
public final class LoggerController {

  private final Message messages;

  LoggerController(final Message messages) {
    this.messages = messages;
  }

  /**
   * Retrieves all loggers and their current levels.
   *
   * @return map of logger names to levels
   */
  @Operation(summary = "List loggers", description = "Retrieves all active loggers and their current configured levels.")
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getAll() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    Map<String, String> loggers = new HashMap<>();

    for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
      if (logger.getLevel() != null) {
        String name = logger.getName();
        if (Logger.ROOT_LOGGER_NAME.equals(name)) {
          name = messages.get(Constant.Message.Core.TXT_ROOT);
        }
        loggers.put(name, logger.getLevel().toString());
      }
    }

    return Map.of(messages.get(Constant.Message.Management.MSG_ALL_LOGGERS),
        loggers);
  }

  /**
   * Retrieves details of a specific logger.
   *
   * @param name the logger name
   * @return logger details including effective level
   */
  @Operation(summary = "Get logger", description = "Retrieves the configured and effective level of a specific logger.")
  @Get("/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getByName(final String name) {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    String loggerName = messages.get(Constant.Message.Core.TXT_ROOT).equalsIgnoreCase(name)
        ? Logger.ROOT_LOGGER_NAME
        : name;

    ch.qos.logback.classic.Logger logger = context.getLogger(loggerName);

    if (logger == null || (logger.getLevel() == null && !Logger.ROOT_LOGGER_NAME.equals(loggerName))) {
      return Map.of(Constant.Code.NOT_FOUND, messages.get(Constant.Message.Management.ERR_LOGGER_NOT_FOUND));
    }

    return Map.of(messages.get(Constant.Message.Management.MSG_LOGGER_DETAILS),
        Map.of(Constant.Key.NAME, name, Constant.Key.LEVEL,
            logger.getEffectiveLevel().toString()));
  }

  /**
   * Updates the logging level of a specific logger.
   *
   * @param name the logger name to update
   * @param body the new level (e.g., DEBUG, INFO, WARN, ERROR)
   * @return update confirmation
   */
  @Operation(summary = "Update logger level", description = "Updates the logging level of a specific logger at runtime.")
  @Put("/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> update(final String name, @ApiBody final String body) {

    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    String loggerName = messages.get(Constant.Message.Core.TXT_ROOT).equalsIgnoreCase(name)
        ? Logger.ROOT_LOGGER_NAME
        : name;

    ch.qos.logback.classic.Logger logger = context.getLogger(loggerName);
    String oldLevel = logger.getLevel() != null ? logger.getLevel().toString() : "INHERITED";

    Level level = Level.toLevel(body, null);
    if (level == null) {
      return Map.of(Constant.Code.BAD_REQUEST,
          messages.get(Constant.Message.Management.ERR_INVALID_LOG_LEVEL));
    }

    logger.setLevel(level);

    return Map.of(messages.get(Constant.Message.Management.MSG_LOGGER_UPDATED),
        Map.of(
            Constant.Key.NAME, name,
            Constant.Key.OLD_LEVEL, oldLevel,
            Constant.Key.NEW_LEVEL, level.toString()));
  }
}
