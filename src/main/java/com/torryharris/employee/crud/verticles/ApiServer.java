package com.torryharris.employee.crud.verticles;


import com.torryharris.employee.crud.model.Employee;
import com.torryharris.employee.crud.model.Response;
import com.torryharris.employee.crud.model.ResponseCodec;
import com.torryharris.employee.crud.util.ConfigKeys;
import com.torryharris.employee.crud.util.PropertyFileUtils;
import com.torryharris.employee.crud.util.Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Base64;


public class ApiServer extends AbstractVerticle {
  private static final Logger logger = LogManager.getLogger(ApiServer.class);
  private static Router router;
  private EmployeeServer employeeServer;


  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    employeeServer = new EmployeeServer(vertx);
    router = Router.router(vertx);


    router.route().handler(BodyHandler.create(false));

    /********************************************************************************/
    router.get("/employees")
      .handler(routingContext -> {
        vertx.eventBus(). request("getall", null,msg->{
        if(msg.succeeded()){
          routingContext.response()
            .setStatusCode(200)
              .putHeader("content-type","application/json")
            .end(msg.result().body().toString());
          }else{
          routingContext.response().setStatusCode(400)
            .end("Failed to get Employee's list");
           }
        });
      });

    router.post("/employee")
      .handler(routingContext -> {
          Employee employee = Json.decodeValue(routingContext.getBody(), Employee.class);
       //   System.out.println(employee);
          vertx.eventBus().request("addemployee", (Json.encode(employee)) , reply -> {
            if (reply.succeeded()) {
              HttpServerResponse serverResponse = routingContext.response();
              serverResponse.putHeader("content-type", "application/json")
                .end(reply.result().body().toString());
            } else {
              routingContext.response().setStatusCode(400)
                .end("failed to add employee");
            }
          });
      });

    router.get("/employee/:id")
      .handler(routingContext -> {
    String id = routingContext.request().getParam("id");
        vertx.eventBus().request("getbyid",id,res-> {
          if(res.succeeded()){
            HttpServerResponse serverResponse = routingContext.response();
            serverResponse.putHeader("content-type","application/json")
              .end( res.result().body().toString());
          }else{
            HttpServerResponse serverResponse = routingContext.response();
            ReplyException exception= (ReplyException) res.cause();
            serverResponse.setStatusCode(exception.failureCode());
            serverResponse.putHeader("content-type","application/json")
              .end(Utils.getErrorResponse(exception.getMessage()).encode());
          }
        });
      });


    router.put("/employee/:id")
      .handler(routingContext -> {
  Employee employee = Json.decodeValue(routingContext.getBody(), Employee.class);
        vertx.eventBus().request("update", (Json.encode(employee)), reply -> {
          if (reply.succeeded()) {
            HttpServerResponse serverResponse = routingContext.response();
            serverResponse.putHeader("content-type","application/json")
              .end(reply.result().body().toString());
          } else {
            HttpServerResponse serverResponse = routingContext.response();
            ReplyException exception= (ReplyException) reply.cause();
            serverResponse.setStatusCode(exception.failureCode());
            serverResponse.putHeader("content-type","application/json")
              .end(Utils.getErrorResponse(exception.getMessage()).encode());
          }
        });
      });


    router.delete("/employee/:id")
      .handler(routingContext -> {
        long id = Long.parseLong(routingContext.request().getParam("id"));
        vertx.eventBus().request("delbyid",id,res->{
          if(res.succeeded()){
            HttpServerResponse serverResponse = routingContext.response();
            serverResponse.putHeader("content-type","application/json")
              .end("Employee deleted with id: "+ id );
          }else{
            HttpServerResponse serverResponse = routingContext.response();
            ReplyException exception= (ReplyException) res.cause();
           serverResponse.setStatusCode(exception.failureCode());
            serverResponse.putHeader("content-type","application/json")
            .end(Utils.getErrorResponse(exception.getMessage()).encode());
          }
        });
        });

     router.get("/login")
      .handler(routingContext -> {
          String authuser = routingContext.request().getHeader(HttpHeaders.AUTHORIZATION);
          //Basic S2VlcnRoaTpLQDEyMw==
          authuser = authuser.substring(6);
          String s = new String(Base64.getDecoder().decode(authuser));
          System.out.println(s);
          String[] arrSpli = s.split(":");
          String username = arrSpli[0];
          String password = arrSpli[1];

          System.out.println(username);
          System.out.println(password);

          vertx.eventBus().request("login",(Json.encode(s)), reply -> {
       if(reply.succeeded()){
           HttpServerResponse serverResponse = routingContext.response();
           serverResponse.putHeader("content-type", "application/json")
             .end(reply.result().body().toString());
       }else {
             System.out.println("failed to login");
       }
          });
        });


    EventBus eventBus= getVertx().eventBus();

    eventBus.registerDefaultCodec(Response.class,new ResponseCodec());

    router.get("/response/:id").handler(routingContext->{
    String id = routingContext.request().getParam("id");
      vertx.eventBus().request("responsebyid",id,reply->{
        if(reply.succeeded()){
         Response response = (Response) reply.result().body();
         //      String response= ((String) reply.result().body());
         //  System.out.println(Json.encode(response));
          HttpServerResponse serverResponse = routingContext.response();
          serverResponse.setStatusCode(response.getStatusCode());
          response.getHeaders().forEach(entry -> {
            serverResponse.putHeader(entry.getKey(),entry.getValue().toString());
          });
          serverResponse.end(response.getResponseBody());
        }else{
          HttpServerResponse serverResponse = routingContext.response();
          ReplyException exception= (ReplyException) reply.cause();
          serverResponse.setStatusCode(exception.failureCode());
          serverResponse.putHeader("content-type","application/json")
            .end(Utils.getErrorResponse(exception.getMessage()).encode());
        }
      });
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

