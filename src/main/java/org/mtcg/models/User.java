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
  private String username = "";

  @JsonIgnore
  private String password = "";
  private String token = "";
  private String bio = "";
  private String image = "";
  private String name = "";
  private int coins = 20;
  private int elo = 100;
  private int wins = 0;
  private int losses = 0;

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
  public User(final UUID id, final String username, final String name, final String bio, final String image,
      final String password,
      final int coins, final String token, final int elo, final int wins, final int losses) {
    this.id = id;
    this.username = username;
    this.name = name;
    this.bio = bio;
    this.image = image;
    this.password = password;
    this.coins = coins;
    this.token = token;
    this.elo = elo;
    this.wins = wins;
    this.losses = losses;
  }

  // Method to verify the password during login
  @JsonIgnore
  public boolean verifyPassword(final String password) {
    return BCrypt.checkpw(password, this.password);
  }

  // Method to hash the password
  private String hashPassword(final String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }
}
