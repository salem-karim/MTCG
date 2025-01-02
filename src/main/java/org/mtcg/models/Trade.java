package org.mtcg.models;

import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class Trade {

  @JsonProperty("Id")
  private final UUID id;
  @JsonProperty("CardToTrade")
  private final UUID cardId;
  @JsonProperty("Type")
  private final String requiredType;
  @JsonProperty("MinimumDamage")
  private final double minDamage;
  private final UUID userId;

  public Trade(final UUID id, final UUID cardId, final String requiredType, final double minDamage, final UUID userId) {
    this.id = id;
    this.cardId = cardId;
    this.requiredType = requiredType;
    this.minDamage = minDamage;
    this.userId = userId;
  }

  @JsonCreator
  public Trade(@JsonProperty("Id") final UUID id,
      @JsonProperty("CardToTrade") final UUID cardId,
      @JsonProperty("Type") final String requiredType,
      @JsonProperty("MinimumDamage") final double minDamage) {
    this(id, cardId, requiredType, minDamage, null);
  }

  // Builder or method to set the userId
  public Trade withUserId(UUID userId) {
    return new Trade(this.id, this.cardId, this.requiredType, this.minDamage, userId);
  }
}
