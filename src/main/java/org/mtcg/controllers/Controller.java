package org.mtcg.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

@Getter
public abstract class Controller {
  protected final ObjectMapper objectMapper;

  public Controller() {
    this.objectMapper = new ObjectMapper();
  }
}
