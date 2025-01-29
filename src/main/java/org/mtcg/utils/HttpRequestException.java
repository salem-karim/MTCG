// src/main/java/org/mtcg/utils/exceptions/HttpRequestException.java
package org.mtcg.utils;

public class HttpRequestException extends Exception {
  public HttpRequestException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public HttpRequestException(final String message) {
    super(message);
  }
}
