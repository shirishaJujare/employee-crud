package com.torryharris.employee.crud.model;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.JsonObject;


public class ResponseCodec implements MessageCodec<Response, Response> {


  @Override
  public void encodeToWire(Buffer buffer, Response response) {
    JsonObject jsonToEncode = new JsonObject();
    jsonToEncode.put("statusCode",response.getStatusCode());
    jsonToEncode.put("responseBody",response.getResponseBody());
    jsonToEncode.put("headers",response.getHeaders());

buffer.appendBuffer(jsonToEncode.toBuffer());

//    int length = jsonTostring.getBytes().length;
//    buffer.appendInt(length);
//    buffer.appendString(jsonTostring);
  }

  @Override
  public Response decodeFromWire(int pos, Buffer buffer) {
//
//    int _pos = pos;
//
//    // Length of JSON
//    int length = buffer.getInt(_pos);
//
//    // Get JSON string by it`s length
//    // Jump 4 because getInt() == 4 bytes
//    String jsonStr = buffer.getString(_pos+=4, _pos+=length);

    JsonObject contentJson = new JsonObject(buffer);

    // Get fields
    int statusCode = contentJson.getInteger("statusCode");
    String responseBody = contentJson.getString("responseBody");
    JsonObject headers = contentJson.getJsonObject("headers");


    return new Response(statusCode,responseBody,headers);
  }

  @Override
  public Response transform(Response response) {
    return response;
  }

  @Override
  public String name() {

    return this.getClass().getName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }

}
