package org.mtcg.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

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

  private UUID id;
  private String name;
  private float damage;
  private CardType cardType;
  private Element element;

  // Constructor with @JsonProperty annotations on parameters
  public Card(
      @JsonProperty("Id") UUID id,
      @JsonProperty("Name") String name,
      @JsonProperty("Damage") float damage) {
    this.id = id;
    this.name = name;
    this.damage = damage;

    this.cardType = (name.contains("Spell")) ? CardType.SPELL : CardType.MONSTER;
    this.element = name.contains("Water") ? Element.WATER : name.contains("Fire") ? Element.FIRE : Element.NORMAL;
  }

  // Constructor for DB Queries and Testing
  public Card(UUID id, String name, float damage, CardType cardType, Element element) {
    this.id = id;
    this.name = name;
    this.damage = damage;
    this.cardType = cardType;
    this.element = element;
  }

}
