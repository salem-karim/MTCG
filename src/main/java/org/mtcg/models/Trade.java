package org.mtcg.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Trade {

  @JsonProperty("id")
  private final UUID id;
  @JsonProperty("CardToTrade")
  private final UUID cardId;
  @JsonProperty("Type")
  private final String requiredType;
  @JsonProperty("MinimumDamage")
  private final int minDamage;
  private final UUID userId; // No longer ignored

  // Private constructor
  private Trade(final UUID id, final UUID cardId, final String requiredType, final int minDamage, final UUID userId) {
    this.id = id;
    this.cardId = cardId;
    this.requiredType = requiredType;
    this.minDamage = minDamage;
    this.userId = userId;
  }

  // Factory method to create a Trade without userId
  @JsonCreator
  public static Trade fromJson(@JsonProperty("id") final UUID id,
      @JsonProperty("CardToTrade") final UUID cardId,
      @JsonProperty("Type") final String requiredType,
      @JsonProperty("MinimumDamage") final int minDamage) {
    return new Trade(id, cardId, requiredType, minDamage, null);
  }

  // Builder or another method to set the userId
  public Trade withUserId(UUID userId) {
    return new Trade(this.id, this.cardId, this.requiredType, this.minDamage, userId);
  }
}
