package org.mtcg.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginCredentials {
  @JsonProperty("Username")
  private String username = "";
  @JsonProperty("Password")
  private String password = "";
}
