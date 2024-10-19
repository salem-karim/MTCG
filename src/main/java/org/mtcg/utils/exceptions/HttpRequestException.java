// src/main/java/org/mtcg/utils/exceptions/HttpRequestException.java
package org.mtcg.utils.exceptions;

public class HttpRequestException extends Exception {
  public HttpRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
