package org.mtcg.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserStats {
  @JsonProperty("Name")
  private String name;

  @JsonProperty("Elo")
  private int elo;

  @JsonProperty("Wins")
  private int wins;

  @JsonProperty("Losses")
  private int losses;

  // Constructor to create from User (and potentially a Stats object if you have
  // one)
  public static UserStats fromUser(User user, int wins, int losses) {
    UserStats stats = new UserStats();
    stats.name = user.getUsername();
    stats.elo = user.getElo();
    stats.wins = wins;
    stats.losses = losses;
    return stats;
  }
}
