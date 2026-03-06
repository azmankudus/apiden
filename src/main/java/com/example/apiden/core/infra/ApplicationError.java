package com.example.apiden.core.infra;

import java.util.List;

/**
 * Custom exception class for representing application-specific errors.
 * 
 * <p>It carries an error code and optional arguments for message formatting.</p>
 */
public final class ApplicationError extends Exception {

  private final String code;
  private final List<Object> args;

  /**
   * Constructs an ApplicationError with a specific error code.
   *
   * @param code The machine-readable error code.
   */
  public ApplicationError(final String code) {
    this.code = code;
    this.args = null;
  }

  /**
   * Constructs an ApplicationError with a code and formatting arguments.
   *
   * @param code The machine-readable error code.
   * @param args Arguments to populate placeholders in the error message.
   */
  public ApplicationError(final String code, final List<Object> args) {
    this.code = code;
    this.args = args;
  }

  /**
   * Constructs an ApplicationError with a code, arguments, and an underlying cause.
   *
   * @param code  The machine-readable error code.
   * @param args  Arguments for localization placeholders.
   * @param cause The underlying throwable that caused this error.
   */
  public ApplicationError(final String code, final List<Object> args, final Throwable cause) {
    super(cause);
    this.code = code;
    this.args = args;
  }

  /**
   * Returns the error code.
   *
   * @return The error code string.
   */
  public String getCode() {
    return code;
  }

  /**
   * Returns the formatting arguments.
   *
   * @return A list of objects used for message formatting, or null if none.
   */
  public List<Object> getArgs() {
    return args;
  }
}
