package com.example.apiden.feature.hello;

import com.example.apiden.core.infra.Constant;
import com.example.apiden.core.infra.Message;
import com.example.apiden.core.web.ApiBody;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.MediaType;

@Controller("/hello")
public final class HelloController {

  private final Message messages;

  HelloController(final Message messages) {
    this.messages = messages;
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public Hello index() throws Exception {
    return new Hello(messages.get(Constant.Message.Hello.TXT_HELLO));
  }

  @Post
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Hello echo(@ApiBody final Hello hello) throws Exception {
    return new Hello(new StringBuilder(hello.message()).reverse().toString());
  }
}
