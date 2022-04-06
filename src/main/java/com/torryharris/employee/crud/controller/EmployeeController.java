package com.torryharris.employee.crud.controller;

import com.torryharris.employee.crud.dao.Dao;
import com.torryharris.employee.crud.dao.impl.EmployeeJdbcDao;
import com.torryharris.employee.crud.model.Employee;
import com.torryharris.employee.crud.model.Response;
import com.torryharris.employee.crud.service.ValidatorService;
import com.torryharris.employee.crud.util.Utils;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EmployeeController {
  private static final Logger LOGGER = LogManager.getLogger(EmployeeController.class);
  private final Dao<Employee> employeeDao;
  private final ValidatorService<Employee> employeeValidator;

  public EmployeeController(Vertx vertx) {
    employeeDao = new EmployeeJdbcDao(vertx);
    employeeValidator = new ValidatorService<>();
  }

  public Promise<Response> getEmployees() {
    Promise<Response> responsePromise = Promise.promise();
    Response response = new Response();
    employeeDao.getAll()
      .future()
      .onSuccess(employees -> {
        response.setStatusCode(200).setResponseBody(Json.encode(employees));
        responsePromise.tryComplete(response);
      }).onFailure(throwable -> {
        LOGGER.catching(throwable);
        response.setStatusCode(500).setResponseBody(Utils.getErrorResponse(throwable.getMessage()).toString());
        responsePromise.tryComplete(response);

      });
    return responsePromise;
  }

  public Promise<Response> getEmployeeById(long id) {
    Promise<Response> responsePromise = Promise.promise();
    Response response = new Response();
    employeeDao.get(id)
      .future()
      .onSuccess(empOptional -> {
        if (empOptional.isPresent()) {
          Employee employee = empOptional.get();
          response.setStatusCode(200)
            .setResponseBody(Json.encode(employee));
        } else {
          response.setStatusCode(400)
            .setResponseBody(Utils.getErrorResponse("Employee with ID :" + id + " not found").encode());
        }
        responsePromise.tryComplete(response);
      }).onFailure(throwable -> {
        LOGGER.catching(throwable);
        response.setStatusCode(500).setResponseBody(Utils.getErrorResponse(throwable.getMessage()).encode());
        responsePromise.tryComplete(response);
      });
    return responsePromise;
  }

  public Promise<Response> updateEmployee(Employee employee) {
    Promise<Response> responsePromise = Promise.promise();
    Response response = new Response();

    JsonObject validationResponse = employeeValidator.validate(employee);
    if (!validationResponse.isEmpty()) {
      response.setStatusCode(200)
        .setResponseBody(validationResponse.encode());
      responsePromise.tryComplete(response);
    } else {
      employeeDao.update(employee)
        .future()
        .onSuccess(noRes -> {
          response.setStatusCode(200).setResponseBody(Utils.getJsonMessage("Updated record successfully").encode());
          responsePromise.tryComplete(response);
        }).onFailure(throwable -> {
          LOGGER.catching(throwable);
          response.setStatusCode(500).setResponseBody(Utils.getErrorResponse(throwable.getMessage()).encode());
          responsePromise.tryComplete(response);
        });
    }
    return responsePromise;
  }

  public Promise<Response> createEmployee(Employee employee) {
    Promise<Response> promise = Promise.promise();
    Response response = new Response();
    JsonObject validationResponse = employeeValidator.validate(employee);
    if (!validationResponse.isEmpty()) {
      response.setStatusCode(200)
        .setResponseBody(validationResponse.encode());
      promise.tryComplete(response);
    } else {
      employeeDao.save(employee)
        .future()
        .onSuccess(employeeRes -> {
          response.setStatusCode(200)
            .setResponseBody(Json.encode(employeeRes));
          promise.tryComplete(response);
        }).onFailure(throwable -> {
          LOGGER.catching(throwable);
          response.setStatusCode(500).setResponseBody(Utils.getErrorResponse(throwable.getMessage()).encode());
          promise.tryComplete(response);
        });
    }
    return promise;
  }

  public Promise<Response> deleteEmployee(long id) {
    Promise<Response> responsePromise = Promise.promise();
    Response response = new Response();
    employeeDao.delete(id).future().onSuccess(rowCount -> {
      if (rowCount > 0) {
        response.setStatusCode(200).setResponseBody(Utils.getJsonMessage("Deleted employee with ID " + id).encode());
      } else {
        response
          .setStatusCode(400)
          .setResponseBody(Utils.getErrorResponse("Employee with ID:" + id + " not found").encode());
      }

      responsePromise.tryComplete(response);
    }).onFailure(throwable -> {
      LOGGER.catching(throwable);
      response.setStatusCode(500).setResponseBody(Utils.getErrorResponse(throwable.getMessage()).encode());
      responsePromise.tryComplete(response);
    });
    return responsePromise;
  }
}
