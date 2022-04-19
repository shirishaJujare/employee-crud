package com.torryharris.employee.crud.util;

import com.torryharris.employee.crud.model.Employee;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Utils {
  private static final Logger LOGGER = LogManager.getLogger(Utils.class);

  private Employee employee;
  /**
   * Get an error response object with the given error message
   * @param errorMessage error message to set
   * @return {@link JsonObject}
   */
  public static JsonObject getErrorResponse(String errorMessage) {
    return new JsonObject()
      .put("error", new JsonObject().put("message", errorMessage));
  }

  public static JsonObject getJsonMessage(String message) {
    return new JsonObject()
      .put("message", message);
  }


  public  static List<Employee> getConnection(long id){
    List<Employee> employees = new ArrayList<>();
    Employee emp=new Employee();
    Connection connection=null;
    try{
      Class.forName("org.mariadb.jdbc.Driver");
      connection = DriverManager.getConnection("jdbc:mariadb://localhost/employee","root","root@123");
      Statement stmt= connection.createStatement();
     // ResultSet res= stmt.executeQuery("SELECT * from Employee");
      ResultSet res= stmt.executeQuery("SELECT * from employee where id="+id);
      while(res.next()){
        emp.setId(res.getLong("id"));
        emp.setName(res.getString("name"));
        emp.setDesignation(res.getString("designation"));
        emp.setSalary(res.getDouble("salary"));
        emp.setUsername(res.getString("username"));
        emp.setPassword(res.getString("password"));
        employees.add(emp);
        LOGGER.info(emp);
      }
      System.out.println("connected");
    }catch (Exception e){
      e.printStackTrace();
    }

    return  employees;
  }
}
