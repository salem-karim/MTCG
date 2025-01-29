package org.mtcg.utils;

public enum ContentType {
  PLAIN_TEXT("text/plain"),
  HTML("text/html"),
  JSON("application/json");

  public final String type;

  ContentType(final String type) {
    this.type = type;
  }
}
