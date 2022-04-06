package com.torryharris.employee.crud.verticles;

import com.torryharris.employee.crud.controller.EmployeeController;
import com.torryharris.employee.crud.controller.LoginController;
import com.torryharris.employee.crud.model.Employee;
import com.torryharris.employee.crud.model.Response;
import com.torryharris.employee.crud.util.ConfigKeys;
import com.torryharris.employee.crud.util.PropertyFileUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ApiServer extends AbstractVerticle {
  private static final Logger logger = LogManager.getLogger(ApiServer.class);
  private static Router router;
  private EmployeeController employeeController;
  private LoginController loginController;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    employeeController = new EmployeeController(vertx);
    loginController = new LoginController(vertx);

    router = Router.router(vertx);

    // Attach a BodyHandler to parse request body and set upload to false
    router.route().handler(BodyHandler.create(false))
      .handler(routingCxt -> {
        logger.info("[{}] [{}]", routingCxt.request().method().toString(), routingCxt.request().path());
        routingCxt.next();
      });

    router.get("/login")
      .handler(routingCxt -> {
        MultiMap header = routingCxt.request().headers();
        if (header.contains(HttpHeaders.AUTHORIZATION)) {
          String encodedCredentials = header.get(HttpHeaders.AUTHORIZATION);
          String credentials = new String(Base64.getDecoder().decode(encodedCredentials.split("\\s")[1]),
            StandardCharsets.UTF_8);
          String username = credentials.split(":")[0];
          String password = credentials.split(":")[1];
          loginController.login(username, password)
            .future()
            .onSuccess(response -> sendResponse(routingCxt, response));
        } else {
          Response response = new Response();
          response.setStatusCode(HttpResponseStatus.UNAUTHORIZED.code());
          sendResponse(routingCxt, response);
        }
      });

    router.get("/employees")
      .handler(routingContext -> employeeController.getEmployees().future()
        .onSuccess(response -> sendResponse(routingContext, response))
      );

    router.get("/employees/:id")
      .handler(routingCxt -> {
        long id = Long.parseLong(routingCxt.request().getParam("id"));
        employeeController.getEmployeeById(id)
          .future()
          .onSuccess(response -> sendResponse(routingCxt, response));
      });

    router.put("/employees")
      .handler(routingCxt -> {
        Employee employee = Json.decodeValue(routingCxt.getBodyAsString(), Employee.class);
        employeeController.updateEmployee(employee)
          .future()
          .onSuccess(response -> sendResponse(routingCxt, response));
      });

    router.post("/employees")
      .handler(routingCxt -> {
        Employee employee;
        try {
          employee = Json.decodeValue(routingCxt.getBodyAsString(), Employee.class);
          employeeController.createEmployee(employee)
            .future()
            .onSuccess(response -> sendResponse(routingCxt, response));
        } catch (Exception e) {
          logger.catching(e);
          Response response = new Response().setStatusCode(500)
            .setResponseBody(e.getMessage());
          sendResponse(routingCxt, response);
        }

      });

    router.delete("/employees/:id")
      .handler(routingCxt -> {
        long id = Long.parseLong(routingCxt.request().getParam("id"));
        employeeController.deleteEmployee(id)
          .future()
          .onSuccess(response -> sendResponse(routingCxt, response));
      });

    HttpServerOptions options = new HttpServerOptions().setTcpKeepAlive(true);
    vertx.createHttpServer(options)
      .exceptionHandler(logger::catching)
      .requestHandler(router)
      .listen(Integer.parseInt(PropertyFileUtils.getProperty(ConfigKeys.HTTP_SERVER_PORT)))
      .onSuccess(httpServer -> {
        logger.info("Server started on port {}", httpServer.actualPort());
        startPromise.tryComplete();
      })
      .onFailure(startPromise::tryFail);
  }

  private void sendResponse(RoutingContext routingContext, Response response) {
    response.getHeaders().stream()
      .forEach(entry -> routingContext.response().putHeader(entry.getKey(), entry.getValue().toString()));
    routingContext.response().setStatusCode(response.getStatusCode())
      .end(response.getResponseBody());
  }
}
