package com.example.apiden.core.infra;

import java.util.List;

public final class ApplicationError extends Exception {

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
