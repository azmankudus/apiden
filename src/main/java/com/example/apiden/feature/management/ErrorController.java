package com.example.apiden.feature.management;

import com.example.apiden.core.infra.ApplicationError;
import com.example.apiden.core.web.ApiBody;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.MediaType;

@Controller("/error")
public final class ErrorController {

  ErrorController() {
  }

  @Get
  @Produces(MediaType.APPLICATION_JSON)
  public String index() throws Exception {
    throw new ApplicationError("ERR-0001");
  }

  @Post
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public String echo(@ApiBody final String body) throws Exception {
    throw new Exception("ERR-0002");
  }
}
