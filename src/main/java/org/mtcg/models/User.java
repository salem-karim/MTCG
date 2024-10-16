package org.mtcg.models;

import lombok.Getter;

@Getter
public class User {
  private final String token;
  private final String username;
  private final String password;
  private int coins;

  public User(String name, String passwrd) {
    token = name + "-mtcgTocken";
    username = name;
    password = passwrd;
    coins = 20;
  }
}
