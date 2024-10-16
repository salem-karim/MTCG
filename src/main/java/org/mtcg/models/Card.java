package org.mtcg.models;

import lombok.Getter;

@Getter
public class Card {
  enum cardType {
    MONSTER,
    SPELL,
    NORMAL
  }
  enum element {
    WATER,
    FIRE,
    NORMAL
  }
  private final String Id;
  private final String name;
  private final float damage;
  private final cardType cardType;
  private final element element;

  public Card(String id, String name, element element, cardType cardType, float damage) {
    Id = id;
    this.name = name;
    this.element = element;
    this.cardType = cardType;
    this.damage = damage;
  }
}
