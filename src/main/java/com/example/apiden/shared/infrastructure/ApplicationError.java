package com.example.apiden.shared.infrastructure;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ApplicationError extends Exception {

  private static final Logger logger = LoggerFactory.getLogger(ApplicationError.class);

  private final String code;
  private final List<Object> args;

  public ApplicationError(final String code) {
    this.code = code;
    this.args = null;
  }

  public ApplicationError(final String code, final List<Object> args) {
    this.code = code;
    this.args = args;
  }

  public ApplicationError(final String code, final List<Object> args, final Throwable cause) {
    super(cause);
    this.code = code;
    this.args = args;
  }

  public String getCode() {
    return code;
  }

  public List<Object> getArgs() {
    return args;
  }
}
