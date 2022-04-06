package com.torryharris.employee.crud.dao.impl;

import com.torryharris.employee.crud.model.Employee;
import com.torryharris.employee.crud.service.JdbcDbService;
import com.torryharris.employee.crud.util.PropertyFileUtils;
import com.torryharris.employee.crud.util.QueryNames;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.util.Optional;

public class LoginDao {
  private final JDBCPool jdbcPool;

  public LoginDao(Vertx vertx) {
    jdbcPool = JdbcDbService.getInstance(vertx).getJdbcPool();
  }

  public Promise<Optional<Employee>> login(String username, String password) {
    Promise<Optional<Employee>> responsePromise = Promise.promise();
    jdbcPool.preparedQuery(PropertyFileUtils.getQuery(QueryNames.LOGIN))
      .execute(Tuple.of(username, password))
      .onSuccess(rows -> {
        if (rows.size() > 0) {
          Employee employee = new Employee();
          for (Row row : rows) {
            employee.setId(row.getLong("id"))
              .setName(row.getString("name"))
              .setUsername(username)
              .setDesignation(row.getString("designation"));
            break;
          }
          responsePromise.tryComplete(Optional.of(employee));
        } else {
          responsePromise.tryComplete(Optional.empty());
        }
      })
      .onFailure(responsePromise::tryFail);
    return responsePromise;
  }
}
