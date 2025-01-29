package org.mtcg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.Getter;

@Getter
public abstract class Controller {
  private static final boolean DEBUG = true;
  protected final ObjectMapper objectMapper;

  public Controller() {
    this.objectMapper = new ObjectMapper();

    if (DEBUG) {
      objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    } else {
      objectMapper.disable(SerializationFeature.INDENT_OUTPUT);
    }
  }

  protected String createJsonMessage(final String attribute, final String message) {
    return String.format("{\"%s\": \"%s\"}\n", attribute, message);
  }
}
