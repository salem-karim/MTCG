package org.mtcg.httpserver;

import lombok.Getter;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

@Getter
public class HttpResponse {
  private final int statusCode;
  private final int contentLength;
  private final String statusMessage;
  private final String contentType;
  private final String body;
  private final String version;
  private final String host;

  public HttpResponse(final HttpStatus status, final ContentType contentType, final String body) {
    this.statusCode = status.code;
    this.contentType = contentType.toString();
    this.body = body;
    this.statusMessage = status.toString();
    this.version = "HTTP/1.1";
    this.host = "127.0.0.1";
    this.contentLength = body.length();
  }

  @Override
  public String toString() {
    return version + " " + statusCode + " " + statusMessage + "\r\n" +
        "Host: " + host + "\r\n" +
        "Content-type: " + contentType + "\r\n" +
        "Content-length: " + contentLength + "\r\n" +
        "\r\n" + body + "\n\n";
  }
}
