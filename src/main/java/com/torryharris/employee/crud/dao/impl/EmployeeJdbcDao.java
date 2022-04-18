package com.torryharris.employee.crud.dao.impl;

import com.torryharris.employee.crud.dao.Dao;
import com.torryharris.employee.crud.model.Employee;
import com.torryharris.employee.crud.service.JdbcDbService;
import com.torryharris.employee.crud.util.ConfigKeys;
import com.torryharris.employee.crud.util.PropertyFileUtils;
import com.torryharris.employee.crud.util.QueryNames;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class EmployeeJdbcDao implements Dao<Employee> {
  private static final Logger LOGGER = LogManager.getLogger(EmployeeJdbcDao.class);
  private JDBCPool jdbcPool;
  private AuthenticationProvider authProvider;

  public EmployeeJdbcDao(Vertx vertx) {
    jdbcPool = JdbcDbService.getInstance(vertx, getJdbcConnectionOptions(), getPoolOptions()).getJdbcPool();
  }


  @Override
  public Promise<List<Employee>> login(String username, String password) {
    Promise<List<Employee>> promises = Promise.promise();
    List<Employee> employees = new ArrayList<>();
    LOGGER.info(username);
    LOGGER.info(password);
    Employee employee = new Employee();
    jdbcPool.preparedQuery(PropertyFileUtils.getQuery(QueryNames.LOGIN))
      .execute(Tuple.of(username,password))
      .onSuccess(rows -> {
        for (Row row : rows) {
          employee.setId(row.getLong("id"))
            .setUsername(row.getString("username"))
            .setName(row.getString("name"))
            .setDesignation(row.getString("designation"));

          employees.add(employee);
        }
        promises.tryComplete(employees);
      });
    return promises;
  }

  @Override
  public Promise<List<Employee>> get(String id) {
    Promise<List<Employee>> promises = Promise.promise();
    List<Employee> employees = new ArrayList<>();
    jdbcPool.preparedQuery(PropertyFileUtils.getQuery(QueryNames.GET_BY_ID))
      .execute(Tuple.of(id))
      .onSuccess(rows -> {
        for (Row row : rows) {
          Employee employee = new Employee();
          employee.setId(row.getLong("id"))
            .setName(row.getString("name"))
            .setDesignation(row.getString("designation"))
            .setSalary(row.getLong("salary"))
            .setUsername(row.getString("username"))
            .setPassword(row.getString("password"));

          employees.add(employee);
        }
        promises.tryComplete(employees);
      });
    return promises;
  }

  @Override
  public Promise<List<Employee>> getAll() {
    Promise<List<Employee>> promise = Promise.promise();
    List<Employee> employees = new ArrayList<>();
    jdbcPool
      .query(PropertyFileUtils.getQuery(QueryNames.GET_ALL_EMPLOYEES))
      .execute()
      .onSuccess(rows -> {
        for (Row row : rows) {
          Employee employee = new Employee();
          employee.setId(row.getLong("id"))
            .setUsername(row.getString("username"))
            .setName(row.getString("name"))
            .setDesignation(row.getString("designation"))
            .setPassword(row.getString("password"))
            .setSalary(row.getLong("salary"));
          employees.add(employee);
        }
        promise.tryComplete(employees);
      });
    return promise;
  }

  @Override
  public void save(Employee e) {
    Promise<List<Employee>> promise = Promise.promise();
    List<Employee> employee = new ArrayList<>();
    Employee emp = new Employee();
    jdbcPool.preparedQuery(PropertyFileUtils.getQuery(QueryNames.INSERT_EMPLOYEE))
      .execute(Tuple.of(e.getId(), e.getUsername(), e.getName(), e.getDesignation(), e.getPassword(), e.getSalary()))
      .onSuccess(rows -> {
        for (Row row : rows) {
          emp.setId(row.getLong("id"))
            .setUsername(row.getString("username"))
            .setName(row.getString("name"))
            .setDesignation(row.getString("designation"))
            .setPassword(row.getString("password"))
            .setSalary(row.getLong("salary"));
          employee.add(emp);
        }
        promise.tryComplete(employee);
      });
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

  public Promise<Integer> update(Employee employee) {
    Promise<Integer> employeePromise = Promise.promise();
    jdbcPool.preparedQuery(PropertyFileUtils.getQuery(QueryNames.UPDATE_EMPLOYEE))
      .execute(Tuple.of( employee.getDesignation(),employee.getSalary(),employee.getId()))
      .onSuccess(rows -> employeePromise.tryComplete(rows.rowCount()))
      .onFailure(employeePromise::tryFail);
    return employeePromise;
  }

  private JDBCConnectOptions getJdbcConnectionOptions() {
    return new JDBCConnectOptions()
      .setJdbcUrl(PropertyFileUtils.getProperty(ConfigKeys.JDBC_URL))
      .setUser(PropertyFileUtils.getProperty(ConfigKeys.JDBC_USERNAME))
      .setPassword(PropertyFileUtils.getProperty(ConfigKeys.JDBC_PASSWORD))
      .setAutoGeneratedKeys(true);
  }

  private PoolOptions getPoolOptions() {
    return new PoolOptions()
      .setMaxSize(Integer.parseInt(PropertyFileUtils.getProperty(ConfigKeys.POOL_SIZE)));
  }
}
