package com.torryharris.employee.crud.service;

import io.vertx.core.json.JsonObject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

public class ValidatorService<T> {
  private final Validator validator;

  public ValidatorService() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    this.validator = factory.getValidator();
  }

  public JsonObject validate(T objectToValidate) {
    Set<ConstraintViolation<T>> constraintViolations = validator.validate(objectToValidate);
    JsonObject response = new JsonObject();

    if (!constraintViolations.isEmpty()) {
      for (ConstraintViolation<T> constraint : constraintViolations) {
        response.put(constraint.getPropertyPath().toString(), constraint.getMessage());
      }
    }
    if (!response.isEmpty()) {
      return new JsonObject().put("message", response);
    }
    return response;
  }
}
