package org.mtcg.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class UserStats {
  @JsonProperty("Username")
  private String username = "";

  @JsonProperty("Elo")
  private int elo = 0;

  @JsonProperty("Wins")
  private int wins = 0;

  @JsonProperty("Losses")
  private int losses = 0;

  @JsonCreator
  public UserStats(@JsonProperty("Username") final String username, @JsonProperty("Elo") final int elo,
      @JsonProperty("Wins") final int wins, @JsonProperty("Losses") final int losses) {
    this.username = username;
    this.elo = elo;
    this.wins = wins;
    this.losses = losses;
  }
}
