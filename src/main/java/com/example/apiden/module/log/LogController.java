package com.example.apiden.module.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.example.apiden.shared.api.ApiBody;
import com.example.apiden.shared.api.ApiConstants;
import com.example.apiden.shared.api.ResponseBody;
import com.example.apiden.shared.api.ResponseStatus;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing log levels dynamically across the application.
 */
@Controller("/log")
public final class LogController {

  private static final Logger log = LoggerFactory.getLogger(LogController.class);

  private final Message messages;

  @Inject
  LogController(final Message messages) {
    this.messages = messages;
  }

  /**
   * Endpoint for retrieving all loggers and their explicit log levels.
   *
   * @return A success response containing all loggers with their levels.
   */
  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<ResponseBody> getAll() {
    log.info("Received request for GET /log");
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    Map<String, String> loggers = new HashMap<>();

    for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
      if (logger.getLevel() != null) {
        String name = logger.getName();
        if (Logger.ROOT_LOGGER_NAME.equals(name)) {
          name = messages.get(ApiConstants.Label.ROOT);
        }
        loggers.put(name, logger.getLevel().toString());
      }
    }

    return HttpResponse.ok(new ResponseBody(
        ResponseStatus.SUCCESS,
        ApiConstants.Code.SUCCESS,
        messages.get(ApiConstants.Msg.ALL_LOGGERS),
        loggers));
  }

  /**
   * Endpoint for retrieving the log level of a specific logger.
   *
   * @param name   The name of the logger (e.g., 'root' or package name).
   * @return The logger name and level in a success response, or Error if not found.
   */
  @Get("/{name}")
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<ResponseBody> getByName(final String name) {
    log.info("Received request for GET /log/{}", name);
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

    String loggerName = messages.get(ApiConstants.Label.ROOT).equalsIgnoreCase(name)
        ? Logger.ROOT_LOGGER_NAME
        : name;

    ch.qos.logback.classic.Logger logger = context.getLogger(loggerName);

    if (logger == null || (logger.getLevel() == null && !Logger.ROOT_LOGGER_NAME.equals(loggerName))) {
      log.warn("Logger not found or has no explicit level: {}", name);
      return HttpResponse.notFound(new ResponseBody(
          ResponseStatus.ERROR,
          ApiConstants.Code.NOT_FOUND,
          messages.get(ApiConstants.Msg.LOGGER_NOT_FOUND),
          null));
    }

    return HttpResponse.ok(new ResponseBody(
        ResponseStatus.SUCCESS,
        ApiConstants.Code.SUCCESS,
        messages.get(ApiConstants.Msg.LOGGER_DETAILS),
        Map.of(ApiConstants.Key.NAME, name, ApiConstants.Key.LEVEL,
            logger.getEffectiveLevel().toString())));
  }

  /**
   * Endpoint for updating or creating a logger level.
   *
   * @param name   The name of the logger.
   * @param body   The new log level string (wrapped in @ApiBody).
   * @return The logger name, old level, and new level in a success response.
   */
  @Put("/{name}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public HttpResponse<ResponseBody> update(final String name, @ApiBody final String body) {
    log.info("Received request for PUT /log/{} with new level {}", name, body);

    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    String loggerName = messages.get(ApiConstants.Label.ROOT).equalsIgnoreCase(name)
        ? Logger.ROOT_LOGGER_NAME
        : name;

    ch.qos.logback.classic.Logger logger = context.getLogger(loggerName);
    String oldLevel = logger.getLevel() != null ? logger.getLevel().toString() : "INHERITED";

    Level level = Level.toLevel(body, null);
    if (level == null) {
      log.warn("Invalid log level provided: {}", body);
      return HttpResponse.badRequest(new ResponseBody(
          ResponseStatus.ERROR,
          ApiConstants.Code.BAD_REQUEST,
          messages.get(ApiConstants.Msg.INVALID_LOG_LEVEL),
          null));
    }

    logger.setLevel(level);
    log.info("Logger '{}' level updated from {} to {}", loggerName, oldLevel, level);

    return HttpResponse.ok(new ResponseBody(
        ResponseStatus.SUCCESS,
        ApiConstants.Code.SUCCESS,
        messages.get(ApiConstants.Msg.LOGGER_UPDATED),
        Map.of(
            ApiConstants.Key.NAME, name,
            ApiConstants.Key.OLD_LEVEL, oldLevel,
            ApiConstants.Key.NEW_LEVEL, level.toString())));
  }
}
