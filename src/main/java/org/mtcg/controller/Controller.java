package org.mtcg.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Controller {
  private ObjectMapper objectMapper;

  public Controller() {
    this.objectMapper = new ObjectMapper();
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }
}
