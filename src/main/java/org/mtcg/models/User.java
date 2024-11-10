package org.mtcg.models;

import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
  @JsonProperty("Id")
  private UUID id;
  @JsonProperty("Username")
  private String username;

  @JsonIgnore
  private String password;
  private int coins = 20;
  private String token;

  // Constructor for ObjectMapper
  @JsonCreator
  public User(@JsonProperty("Username") final String username,
      @JsonProperty("Password") final String password) {
    this.id = UUID.randomUUID();
    this.token = username + "-mtcgToken";
    this.username = username;
    // Hash the password
    this.password = hashPassword(password);
  }

  // Additional constructor for retrieving from the database
  @JsonIgnore
  public User(final String username, final String token, final String Password, final UUID id) {
    this.id = id;
    this.token = token;
    this.username = username;
    this.password = Password;
  }

  // Method to hash the password
  private String hashPassword(final String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  // Method to verify the password during login
  @JsonIgnore
  public boolean verifyPassword(final String password) {
    return BCrypt.checkpw(password, this.password);
  }
}
