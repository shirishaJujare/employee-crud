package com.torryharris.employee.crud.controller;

import com.torryharris.employee.crud.dao.impl.LoginDao;
import com.torryharris.employee.crud.model.Response;
import com.torryharris.employee.crud.util.Utils;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginController {
  private static final Logger LOGGER = LogManager.getLogger(LoginController.class);
  private final LoginDao loginDao;

  public LoginController(Vertx vertx) {
    this.loginDao = new LoginDao(vertx);
  }

  public Promise<Response> login(String username, String password) {
    Promise<Response> responsePromise = Promise.promise();
    Response response = new Response();
    loginDao.login(username, password)
      .future()
      .onSuccess(optionalEmployee -> optionalEmployee.ifPresentOrElse(employee -> {
          response.setStatusCode(200)
            .setResponseBody(Json.encode(employee));
          responsePromise.tryComplete(response);
        },
        () -> {
          response.setStatusCode(400)
            .setResponseBody(Utils.getErrorResponse("Invalid credentials").encode());
          responsePromise.tryComplete(response);
        }
      )).onFailure(throwable -> {
        LOGGER.catching(throwable);
        response.setStatusCode(500).setResponseBody(Utils.getErrorResponse(throwable.getMessage()).toString());
        responsePromise.tryComplete(response);
      });
    return responsePromise;
  }
}
