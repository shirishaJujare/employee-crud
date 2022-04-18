package com.torryharris.employee.crud.util;

import io.vertx.core.json.JsonObject;

public class Utils {

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
}
