package org.mtcg.models;

import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Card {
  public enum CardType {
    MONSTER,
    SPELL,
  }

  public enum Element {
    WATER,
    FIRE,
    NORMAL
  }

  private final UUID id;
  private String name = "";
  private double damage = 0.0;
  private final CardType cardType;
  private final Element element;
  private boolean hasVictoryBoost = false;
  private static final double VICTORY_BOOST = 1.3;

  // Constructor with @JsonProperty annotations on parameters
  public Card(
      @JsonProperty("Id") final UUID id,
      @JsonProperty("Name") final String name,
      @JsonProperty("Damage") final double damage) {
    this.id = id;
    this.name = name;
    this.damage = damage;

    this.cardType = (name.contains("Spell")) ? CardType.SPELL : CardType.MONSTER;
    this.element = name.contains("Water") ? Element.WATER : name.contains("Fire") ? Element.FIRE : Element.NORMAL;
  }

  // Constructor for DB Queries and Testing
  public Card(final UUID id, final String name, final double damage, final CardType cardType, final Element element) {
    this.id = id;
    this.name = name;
    this.damage = damage;
    this.cardType = cardType;
    this.element = element;
  }

  @Override
  public String toString() {
    return "ID: " + id + " Name: " + name +
        "\nDamage: " + damage + " Element/Type: " +
        element + '/' + cardType + '\n';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Card card = (Card) o;
    return Objects.equals(id, card.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  public void tryApplyVictoryBoost() {
    if (!hasVictoryBoost && Math.random() < 0.5) {
      this.damage *= VICTORY_BOOST;
      this.hasVictoryBoost = true;
    }
  }

}
