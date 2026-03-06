package com.example.apiden.module.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.example.apiden.shared.api.ApiBody;
import com.example.apiden.shared.infrastructure.Constant;
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

import java.util.HashMap;
import java.util.Map;

@Controller("/logger")
public final class LoggerController {

  private static final Logger logger = LoggerFactory.getLogger(LoggerController.class);

  private final Message messages;

  @Inject
  LoggerController(final Message messages) {
    this.messages = messages;
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getAll() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    Map<String, String> loggers = new HashMap<>();

    for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
      if (logger.getLevel() != null) {
        String name = logger.getName();
        if (Logger.ROOT_LOGGER_NAME.equals(name)) {
          name = messages.get(Constant.Label.ROOT);
        }
        loggers.put(name, logger.getLevel().toString());
      }
    }

    return Map.of(messages.get(Constant.Msg.ALL_LOGGERS),
        loggers);
  }

  @Get("/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> getByName(final String name) {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    String loggerName = messages.get(Constant.Label.ROOT).equalsIgnoreCase(name)
        ? Logger.ROOT_LOGGER_NAME
        : name;

    ch.qos.logback.classic.Logger logger = context.getLogger(loggerName);

    if (logger == null || (logger.getLevel() == null && !Logger.ROOT_LOGGER_NAME.equals(loggerName))) {
      return Map.of(Constant.Code.NOT_FOUND, messages.get(Constant.Msg.LOGGER_NOT_FOUND));
    }

    return Map.of(messages.get(Constant.Msg.LOGGER_DETAILS),
        Map.of(Constant.Key.NAME, name, Constant.Key.LEVEL,
            logger.getEffectiveLevel().toString()));
  }

  @Put("/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, Object> update(final String name, @ApiBody final String body) {

    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    String loggerName = messages.get(Constant.Label.ROOT).equalsIgnoreCase(name)
        ? Logger.ROOT_LOGGER_NAME
        : name;

    ch.qos.logback.classic.Logger logger = context.getLogger(loggerName);
    String oldLevel = logger.getLevel() != null ? logger.getLevel().toString() : "INHERITED";

    Level level = Level.toLevel(body, null);
    if (level == null) {
      return Map.of(Constant.Code.BAD_REQUEST,
          messages.get(Constant.Msg.INVALID_LOG_LEVEL));
    }

    logger.setLevel(level);

    return Map.of(messages.get(Constant.Msg.LOGGER_UPDATED),
        Map.of(
            Constant.Key.NAME, name,
            Constant.Key.OLD_LEVEL, oldLevel,
            Constant.Key.NEW_LEVEL, level.toString()));
  }
}
