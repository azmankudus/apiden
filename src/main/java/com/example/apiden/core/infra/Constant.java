package com.example.apiden.core.infra;

/**
 * Global registry for technical keys and non-localizable constants.
 * 
 * <p>User-facing strings should NOT be defined here as literals; instead, use the 
 * message keys in {@link Message} to resolve strings via the localization system.</p>
 */
public final class Constant {

  /**
   * Private constructor to prevent instantiation.
   */
  private Constant() {
  }

  /**
   * Configuration property keys used in application.properties or environment variables.
   */
  public static final class Config {
    /**
     * Private constructor for utility class.
     */
    private Config() {
    }

    public static final String INCLUDE_METADATA = "application.api.response.include-metadata";
    public static final String INCLUDE_STACKTRACE = "application.api.response.include-stacktrace";
    public static final String INCLUDE_ENVIRONMENT_INFO = "application.api.response.include-environment-info";
    public static final String LIVE_UPDATE_ENABLED = "application.configuration.live-update.enabled";
    public static final String LIVE_UPDATE_LIST = "application.configuration.live-update.list";
    public static final String CLIENT_USER_AGENT = "application.api.client.user-agent";
  }

  /**
   * Names for request and response attributes used in filters and propagated contexts.
   */
  public static final class Attr {
    /**
     * Private constructor for utility class.
     */
    private Attr() {
    }

    public static final String TIMESTAMP = "server_request_timestamp";
    public static final String REQUEST_DATA = "api_request_data";
    public static final String CONTEXT_TRACE_ID = "traceid";
    public static final String CONTEXT_LANGUAGE = "language";
    public static final String CLIENT_TRACE_ID = "client_trace_id";
    public static final String CLIENT_TIMESTAMP = "client_request_timestamp";
    public static final String CLIENT_USER_AGENT = "client_user_agent";
    public static final String CLIENT_OS = "client_os";
    public static final String CLIENT_IP = "client_ip";
    public static final String CLIENT_ACTUAL_IP = "client_actual_ip";
  }

  /**
   * Standard JSON field names for the API envelope.
   */
  public static final class Envelope {
    /**
     * Private constructor for utility class.
     */
    private Envelope() {
    }

    public static final String DATA = "data";
    public static final String META = "meta";
  }

  /**
   * Standard keys used within the "meta" object of API responses.
   */
  public static final class Meta {
    /**
     * Private constructor for utility class.
     */
    private Meta() {
    }

    public static final String CLIENT_REQUEST_TIMESTAMP = "client_request_timestamp";
    public static final String CLIENT_TRACE_ID = "client_trace_id";
    public static final String SERVER_URL_PATH = "server_url_path";
    public static final String SERVER_REQUEST_TIMESTAMP = "server_request_timestamp";
    public static final String SERVER_REQUEST_TRACE_ID = "server_request_trace_id";
    public static final String SERVER_RESPONSE_TIMESTAMP = "server_response_timestamp";
    public static final String SERVER_RESPONSE_DURATION = "server_response_duration";
    public static final String SERVER_RESPONSE_HTTP_STATUS = "server_response_http_status";
    public static final String SERVER_RESPONSE_EXCEPTION_STACKTRACE = "server_response_exception_stacktrace";
    public static final String CLIENT_USER_AGENT = "client_user_agent";
    public static final String CLIENT_OS = "client_os";
    public static final String CLIENT_IP = "client_ip";
    public static final String CLIENT_ACTUAL_IP = "client_actual_ip";
    public static final String SERVER_INSTANCE_NAME = "server_instance_name";
    public static final String SERVER_OS = "server_os";
    public static final String SERVER_RUNTIME = "server_runtime";
    public static final String SERVER_IP = "server_ip";
  }

  /**
   * Standardized application-level status and error codes.
   */
  public static final class Code {
    /**
     * Private constructor for utility class.
     */
    private Code() {
    }

    public static final String ERROR = "00000500";
    public static final String BAD_REQUEST = "00000400";
    public static final String NOT_FOUND = "00000404";
  }

  /**
   * Hierarchical message keys for the localization system.
   * Pattern: TYPE-ModuleIDMessageID
   */
  public static final class Message {
    /**
     * Private constructor for utility class.
     */
    private Message() {
    }

    /**
     * Core application messages.
     */
    public static final class Core {
      /**
       * Private constructor for utility class.
       */
      private Core() {
      }

      public static final String MSG_SUCCESS = "MSG-00010001";
      public static final String ERR_SERVER = "ERR-00010001";
      public static final String ERR_FORBIDDEN = "ERR-00010002";
      public static final String ERR_APP = "ERR-00010003";
      public static final String TXT_ROOT = "TXT-00010001";
      public static final String TXT_NULL = "TXT-00010002";
    }

    /**
     * Hello module messages.
     */
    public static final class Hello {
      /**
       * Private constructor for utility class.
       */
      private Hello() {
      }

      public static final String TXT_HELLO = "TXT-00100001";
    }

    /**
     * Management module messages.
     */
    public static final class Management {
      /**
       * Private constructor for utility class.
       */
      private Management() {
      }

      public static final String MSG_ALL_CONFIG = "MSG-00200001";
      public static final String MSG_PROP_DETAILS = "MSG-00200002";
      public static final String MSG_PROP_UPDATED = "MSG-00200003";
      public static final String MSG_ALL_LOGGERS = "MSG-00200004";
      public static final String MSG_LOGGER_DETAILS = "MSG-00200005";
      public static final String MSG_LOGGER_UPDATED = "MSG-00200006";
      public static final String ERR_PROP_NOT_FOUND = "ERR-00200001";
      public static final String ERR_INVALID_LOG_LEVEL = "ERR-00200002";
      public static final String ERR_LOGGER_NOT_FOUND = "ERR-00200003";
      public static final String ERR_INTERNAL = "ERR-00200004";
    }
  }

  /**
   * Common keys used in internal maps and JSON payloads.
   */
  public static final class Key {
    /**
     * Private constructor for utility class.
     */
    private Key() {
    }

    public static final String NAME = "name";
    public static final String VALUE = "value";
    public static final String OLD_VALUE = "old_value";
    public static final String NEW_VALUE = "new_value";
    public static final String OLD_LEVEL = "old_level";
    public static final String NEW_LEVEL = "new_level";
    public static final String LEVEL = "level";
  }

  /**
   * Common literal values.
   */
  public static final class Value {
    /**
     * Private constructor for utility class.
     */
    private Value() {
    }

    public static final String UNKNOWN = "Unknown";
  }
}
