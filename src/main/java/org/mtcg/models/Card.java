package org.mtcg.models;

import java.util.UUID;

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

  private final UUID id;
  private final String name;
  private final float damage;
  private final cardType cardType;
  private final element element;

  public Card(final UUID id, final String name, final element element, final cardType cardType, final float damage) {
    this.id = id;
    this.name = name;
    this.element = element;
    this.cardType = cardType;
    this.damage = damage;
  }
}
