package com.torryharris.employee.crud.verticles;


import com.torryharris.employee.crud.dao.Dao;
import com.torryharris.employee.crud.dao.impl.EmployeeJdbcDao;
import com.torryharris.employee.crud.model.Employee;
import com.torryharris.employee.crud.model.Response;
import com.torryharris.employee.crud.util.Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class EmployeeServer extends AbstractVerticle {

  private static final Logger LOGGER = LogManager.getLogger(EmployeeServer.class);
  private Dao<Employee> employeeDao;
  private Employee employeec;
  private  Utils utils;

  public EmployeeServer(Vertx vertx) {

    employeeDao = new EmployeeJdbcDao(vertx);
  }


  @Override
  public void start(Promise<Void> startPromise) throws Exception {


     DeploymentOptions option =new DeploymentOptions().setWorker(true);


      vertx.eventBus().consumer("responsebyid", message -> {
   //  Response id = (Response) message.body();
      String id = (String) message.body();
      Response  response = new Response();
      employeeDao.get(id)
        .future()
        .onSuccess(employee->{
          LOGGER.info(employee);
          response.setStatusCode(200)
              .setResponseBody(employee.toString());
           message.reply(response);
        })
        .onFailure(throwable->{
          message.fail(400, "employee with id: " + id +"Not Found");
          }
        );
    });


    vertx.eventBus().consumer("getbyid", message -> {
  Response response=new Response();
      LOGGER.info(message.body());
      String id = (String) message.body();
      employeeDao.get(id)
        .future()
        .onSuccess(employee -> {
            response.setStatusCode(200);
            message.reply(Json.encode(employee));
            LOGGER.info("Employee received with ID :  " + id + employee);
          })
       .onFailure(throwable -> {
         response.setStatusCode(400)
           .setResponseBody(Utils.getErrorResponse(throwable.getMessage()).toString());
         LOGGER.catching(throwable);
       });
    });


    vertx.eventBus().consumer("addemployee", message -> {
      String msg = ((String) message.body());
      Employee emp = Json.decodeValue(msg,Employee.class) ;
      employeeDao.save(emp);
          message.reply(Json.encode(emp));
    });


    vertx.eventBus().consumer("getall", message -> {
      JsonObject msg = ((JsonObject) message.body());
      employeeDao.getAll()
        .future()
        .onSuccess(employee -> {
          message.reply(Json.encode(employee));
          LOGGER.info(Json.encode(employee));
        });

    });


    vertx.eventBus().consumer("update", message -> {
    String msg = ((String) message.body());
    LOGGER.info(msg);
Employee emp = Json.decodeValue(msg,Employee.class) ;
    employeeDao.update(emp)
        .future()
          .onSuccess(rowcount->{
            if(rowcount == 1){
              message.reply(Utils.getJsonMessage("Employee  updated with Id: " +emp.getId()));
            }else{
              message.fail(400,"employee is not updated");
            }
          });
    });

    vertx.eventBus().consumer("delbyid", message -> {
      long id = (long) message.body();
      employeeDao.delete(id)
        .future()
        .onSuccess(rowCount -> {
          if (rowCount > 0) {
            message.reply(Json.encode(id));
          }else{
            message.fail(400,"employee with id "+ id + " NOT FOUND");
          }
        });
    });


    vertx.eventBus().consumer("login", message -> {
      String msg = (String) message.body();
      LOGGER.info(msg);
      String [] arr= msg.split(":");
      String username = arr[0];
      String password = arr[1];
      LOGGER.info(username);
      LOGGER.info(password);
    });


  }
}
