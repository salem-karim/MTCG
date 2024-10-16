package org.mtcg.models;


import lombok.Getter;

public class User {
  @Getter private final String token;
  @Getter private final String username;
  @Getter private final String password;

  public String getId() {
    return token;
  }
  public String getUsername() {
    return username;
  }
  public String getPassword() {
    return password;
  }
  public User(String token, String username, String password) {
    this.token = token;
    this.username = username;
    this.password = password;
  }
}
