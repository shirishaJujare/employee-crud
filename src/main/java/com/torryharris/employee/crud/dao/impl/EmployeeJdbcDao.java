package com.torryharris.employee.crud.dao.impl;

import com.torryharris.employee.crud.dao.Dao;
import com.torryharris.employee.crud.model.Employee;
import com.torryharris.employee.crud.service.JdbcDbService;
import com.torryharris.employee.crud.util.PropertyFileUtils;
import com.torryharris.employee.crud.util.QueryNames;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeJdbcDao implements Dao<Employee> {
  private final JDBCPool jdbcPool;

  public EmployeeJdbcDao(Vertx vertx) {
    jdbcPool = JdbcDbService.getInstance(vertx).getJdbcPool();
  }

  @Override
  public Promise<Optional<Employee>> get(long id) {
    Promise<Optional<Employee>> optionalPromise = Promise.promise();
    jdbcPool.preparedQuery(PropertyFileUtils.getQuery(QueryNames.GET_EMPLOYEE_BY_ID))
      .execute(Tuple.of(id))
      .onSuccess(rowSet -> {
        Optional<Employee> employeeOptional;
        if (rowSet.size() == 1) {
          Employee employee = new Employee();
          for (Row row : rowSet) {
            employee.setId(id)
              .setName(row.getString("name"))
              .setUsername(row.getString("username"))
              .setDesignation(row.getString("designation"))
              .setSalary(row.getLong("salary"));
            break;
          }
          employeeOptional = Optional.of(employee);
        } else {
          employeeOptional = Optional.empty();
        }
        optionalPromise.tryComplete(employeeOptional);
      })
      .onFailure(optionalPromise::tryFail);
    return optionalPromise;
  }

  @Override
  public Promise<List<Employee>> getAll() {
    Promise<List<Employee>> promise = Promise.promise();
    List<Employee> employees = new ArrayList<>();
    jdbcPool.query(PropertyFileUtils.getQuery(QueryNames.GET_ALL_EMPLOYEES))
      .execute()
      .onSuccess(rows -> {
        for (Row row : rows) {
          Employee employee = new Employee();
          employee.setId(row.getLong("id"))
            .setName(row.getString("name"))
            .setUsername(row.getString("username"))
            .setDesignation(row.getString("designation"))
            .setSalary(row.getLong("salary"));
          employees.add(employee);
        }
        promise.tryComplete(employees);
      });
    return promise;
  }

  @Override
  public Promise<Employee> save(Employee employee) {
    Promise<Employee> promise = Promise.promise();
    jdbcPool.preparedQuery(PropertyFileUtils.getQuery(QueryNames.INSERT_EMPLOYEE))
      .execute(Tuple.of(employee.getName(), employee.getUsername(), employee.getPassword(),
        employee.getDesignation(), employee.getSalary()))
      .onSuccess(rowSet -> {
        // Retrieve the generated keys
        Row lastInsertId = rowSet.property(JDBCPool.GENERATED_KEYS);
        long id = lastInsertId.getLong(0);

        // updated the ID in the employee object
        employee.setId(id);
        promise.tryComplete(employee);
      }).onFailure(promise::tryFail);
    return promise;
  }

  @Override
  public Promise<Integer> update(Employee employee) {
    Promise<Integer> employeePromise = Promise.promise();
    jdbcPool.preparedQuery(PropertyFileUtils.getQuery(QueryNames.UPDATE_EMPLOYEE))
      .execute(Tuple.of(employee.getUsername(), employee.getPassword(), employee.getDesignation(),
        employee.getSalary()))
      .onSuccess(rows -> employeePromise.tryComplete(rows.rowCount()))
      .onFailure(employeePromise::tryFail);
    return employeePromise;
  }

  @Override
  public Promise<Integer> delete(long id) {
    Promise<Integer> promise = Promise.promise();
    jdbcPool.preparedQuery(PropertyFileUtils.getQuery(QueryNames.DELETE_EMPLOYEE))
      .execute(Tuple.of(id))
      .onSuccess(rows -> promise.tryComplete(rows.rowCount()))
      .onFailure(promise::tryFail);
    return promise;
  }
}
