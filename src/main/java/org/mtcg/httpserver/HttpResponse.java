package org.mtcg.httpserver;

import lombok.Getter;

import java.util.Locale;

@Getter
public class HttpResponse {
  private int statusCode;
  private int contentLength;
  private String statusMessage;
  private String contentType;
  private String body;
  private String version;
  private String host;
  private String Response;

  public HttpResponse() {
    this.statusCode = 200;
    this.contentLength = 0;
    this.statusMessage = "OK";
    this.contentType = "application/json";
    this.version = "HTTP/1.1";
    this.host = "127.0.0.1";
    this.body = "";
  }

  public HttpResponse(int code, String message) {
    this();
    statusCode = code;
    body = message.toUpperCase(Locale.ROOT);
    switch(code) {
      case 200: statusMessage = "OK"; break;
      case 201: statusMessage = "Created"; break;
      case 400: statusMessage = "Bad Request"; break;
      case 401: statusMessage = "Unauthorized"; break;
      case 403: statusMessage = "Forbidden"; break;
      case 404: statusMessage = "Not Found"; break;
      case 409: statusMessage = "Conflict"; break;
      case 500: statusMessage = "Internal Server Error"; break;
    }
    StringBuilder builder = new StringBuilder();
    builder.append(version).append(" ").append(statusCode).append(" ").append(statusMessage).append("\r\n");
    builder.append("Host: ").append(host).append("\r\n");
    builder.append("Content-type: ").append(contentType).append("\r\n");
    builder.append("Content-length: ").append(contentLength).append("\r\n");
    builder.append("\r\n").append(this.body).append("\n\n");

    Response = builder.toString();

  }
}
