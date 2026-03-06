package com.example.apiden.shared.infrastructure;

/**
 * Registry for keys and non-localizable constants.
 * User-facing strings should be resolved via MessageSource using the keys
 * defined in ApiConstants.Msg.
 */
public final class Constant {

  private Constant() {
    // Prevent instantiation
  }

  /** Configuration property keys (These are technical, not localized). */
  public static final class Config {
    public static final String INCLUDE_METADATA = "application.api.response.include-metadata";
    public static final String INCLUDE_STACKTRACE = "application.api.response.include-stacktrace";
    public static final String LIVE_UPDATE_ENABLED = "application.configuration.live-update.enabled";
    public static final String LIVE_UPDATE_LIST = "application.configuration.live-update.list";
    public static final String INSTANCE_NAME = "application.instance.name";
    public static final String SERVER_HOST = "micronaut.server.host";
  }

  /** Request and response attribute names (Internal, not localized). */
  public static final class Attr {
    public static final String ENVELOPE = "api_envelope";
    public static final String TIMESTAMP = "server_request_timestamp";
    public static final String REQUEST_DATA = "api_request_data";
    public static final String CONTEXT_TRACE_ID = "traceid";
    public static final String CONTEXT_LANGUAGE = "language";
    public static final String CLIENT_TRACE_ID = "client_trace_id";
    public static final String CLIENT_TIMESTAMP = "client_request_timestamp";
  }

  /** JSON field names for the envelope. */
  public static final class Envelope {
    public static final String DATA = "data";
    public static final String ERRORS = "errors";
    public static final String META = "meta";
  }

  /** Metadata field names. */
  public static final class Meta {
    public static final String CLIENT_REQUEST_TIMESTAMP = "client_request_timestamp";
    public static final String CLIENT_TRACE_ID = "client_trace_id";
    public static final String SERVER_API_URL = "server_api_url";
    public static final String SERVER_REQUEST_TIMESTAMP = "server_request_timestamp";
    public static final String SERVER_REQUEST_TRACE_ID = "server_request_trace_id";
    public static final String SERVER_RESPONSE_TIMESTAMP = "server_response_timestamp";
    public static final String SERVER_RESPONSE_DURATION = "server_response_duration";
    public static final String SERVER_RESPONSE_HTTP_STATUS = "server_response_http_status";
    public static final String SERVER_RESPONSE_EXCEPTION_STACKTRACE = "server_response_exception_stacktrace";
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

  /** Literal keys for localization. */
  public static final class Label {
    public static final String ROOT = "label.root";
    public static final String NULL = "label.null";
    public static final String HELLO_WORLD = "label.hello.world";
    public static final String UNKNOWN = "label.unknown";
  }
}
