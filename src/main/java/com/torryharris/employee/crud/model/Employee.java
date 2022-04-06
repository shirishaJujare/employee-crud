package com.torryharris.employee.crud.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Objects;

public class Employee {

  private long id;

  @NotNull(message = "name cannot be null")
  @Size(min = 1, max = 50, message = "name should not exceed 50 characters.")
  private String name;

  @NotNull(message = "username cannot be null")
  @Size(min = 1, max = 45, message = "username should not exceed 45 characters.")
  private String username;

  @NotNull(message = "password cannot be null")
  @Size(min = 1, max = 100, message = "password should not exceed 45 characters.")
  private String password;

  @NotNull(message = "designation cannot be null")
  @Size(min = 1, max = 50, message = "designation should not exceed 50 characters.")
  private String designation;
  private double salary;

  public long getId() {
    return id;
  }

  public Employee setId(long uid) {
    this.id = uid;
    return this;
  }

  public String getName() {
    return name;
  }

  public Employee setName(String name) {
    this.name = name;
    return this;
  }

  public String getUsername() {
    return username;
  }

  public Employee setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public Employee setPassword(String password) {
    this.password = password;
    return this;
  }

  public String getDesignation() {
    return designation;
  }

  public Employee setDesignation(String designation) {
    this.designation = designation;
    return this;
  }

  public double getSalary() {
    return salary;
  }

  public Employee setSalary(double salary) {
    this.salary = salary;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Employee employee = (Employee) o;
    return id == employee.id && Double.compare(employee.salary, salary) == 0 && name.equals(employee.name)
      && Objects.equals(username, employee.username) && Objects.equals(password, employee.password)
      && Objects.equals(designation, employee.designation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, username, password, designation, salary);
  }
}
