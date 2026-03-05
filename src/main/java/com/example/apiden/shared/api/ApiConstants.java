package com.example.apiden.shared.api;

/**
 * Registry for keys and non-localizable constants.
 * User-facing strings should be resolved via MessageSource using the keys defined in ApiConstants.Msg.
 */
public final class ApiConstants {

  private ApiConstants() {
    // Prevent instantiation
  }

  /** Configuration property keys (These are technical, not localized). */
  public static final class Config {
    public static final String INCLUDE_CLIENT_HEADERS = "application.api.envelope.include-client-headers";
    public static final String INCLUDE_SERVER_HEADERS = "application.api.envelope.include-server-headers";
    public static final String INCLUDE_STACKTRACE = "application.api.envelope.include-stacktrace";
    public static final String INCLUDE_CLIENT_INFO = "application.api.envelope.include-client-info";
    public static final String INCLUDE_SERVER_INFO = "application.api.envelope.include-server-info";
    public static final String LIVE_UPDATE_ENABLED = "application.configuration.live-update.enabled";
    public static final String LIVE_UPDATE_LIST = "application.configuration.live-update.list";
    public static final String INSTANCE_NAME = "application.instance.name";
    public static final String SERVER_HOST = "micronaut.server.host";
  }

  /** Request and response attribute names (Internal, not localized). */
  public static final class Attr {
    public static final String ENVELOPE = "api_envelope";
    public static final String TIMESTAMP = "server_request_timestamp";
    public static final String TRACE_ID = "server_trace_id";
    public static final String CONTEXT_TRACE_ID = "traceid";
    public static final String CONTEXT_LANGUAGE = "language";
    public static final String LOCALE = "server_resolved_locale";
  }

  /** Common status codes. */
  public static final class Code {
    public static final String SUCCESS = "0";
    public static final String ERROR = "500";
    public static final String BAD_REQUEST = "400";
    public static final String FORBIDDEN = "403";
    public static final String NOT_FOUND = "404";
  }

  /** Message keys for localization (To be used with MessageSource). */
  public static final class Msg {
    public static final String SUCCESS = "msg.success";
    public static final String SERVER_ERROR = "msg.server.error";
    public static final String PROPERTY_NOT_FOUND = "msg.property.not.found";
    public static final String PROPERTY_DETAILS = "msg.property.details";
    public static final String PROPERTY_UPDATED = "msg.property.updated";
    public static final String ALL_CONFIGURATION = "msg.all.configuration";
    public static final String ALL_LOGGERS = "msg.all.loggers";
    public static final String LOGGER_DETAILS = "msg.logger.details";
    public static final String LOGGER_UPDATED = "msg.logger.updated";
    public static final String INVALID_LOG_LEVEL = "msg.invalid.log.level"; // parameterized
    public static final String MESSAGE_REQUIRED = "msg.message.required";
    public static final String LOGGER_NOT_FOUND = "msg.logger.not.found";
    public static final String FORBIDDEN = "msg.forbidden";
    public static final String NO_ERROR_MSG = "msg.no.error.msg";
  }

  /** JSON and Map keys (Technical, usually not localized). */
  public static final class Key {
    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String OLD_VALUE = "old_value";
    public static final String NEW_VALUE = "new_value";
    public static final String OLD_LEVEL = "old_level";
    public static final String NEW_LEVEL = "new_level";
    public static final String LEVEL = "level";
    public static final String MESSAGE = "message";
    public static final String STACKTRACE = "stacktrace";
    public static final String EXCEPTION = "exception";
    public static final String STATUS = "status";
    public static final String CODE = "code";
    public static final String BODY = "body";
    public static final String TIMESTAMP = "timestamp";
    public static final String DURATION = "duration";
  }

  /** Keys for Client and Server metadata info blocks. */
  public static final class Info {
    public static final String BROWSER = "browser";
    public static final String OS = "os";
    public static final String IP = "ip";
    public static final String FORWARDED_IP = "forwardedip";
    public static final String INSTANCE = "instance";
    public static final String HOSTNAME = "hostname";
    public static final String RUNTIME = "runtime";
  }

  /** Literal keys for localization. */
  public static final class Label {
    public static final String ROOT = "label.root";
    public static final String NULL = "label.null";
    public static final String HELLO_WORLD = "label.hello.world";
    public static final String UNKNOWN = "label.unknown";
  }
}
