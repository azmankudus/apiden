package com.example.apiden.module.hello;

import com.example.apiden.shared.api.ApiBody;
import com.example.apiden.shared.infrastructure.ApplicationError;
import com.example.apiden.shared.infrastructure.Constant;
import com.example.apiden.shared.infrastructure.Message;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/hello")
public final class HelloController {

  private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

  private final Message messages;

  HelloController(final Message messages) {
    this.messages = messages;
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public Hello index() throws Exception {
    return new Hello(messages.get(Constant.Label.HELLO_WORLD));
  }

  @Post
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Hello echo(@ApiBody final Hello hello) throws Exception {
    return new Hello(new StringBuilder(hello.message()).reverse().toString());
  }
}
