package com.example.apiden.module.infrastructure;

import com.example.apiden.shared.api.ApiBody;
import com.example.apiden.shared.infrastructure.ApplicationError;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/error")
public final class ErrorController {

  private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

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
