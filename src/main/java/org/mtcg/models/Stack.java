package org.mtcg.models;

import java.util.Map;
import java.util.UUID;

import lombok.Getter;

@Getter
public class Stack {
  private final UUID id;
  private final UUID userId;
  private final Map<UUID, Card> cards;

  // Constructor for DB
  public Stack(final UUID id, final UUID userId, final Map<UUID, Card> cards) {
    this.id = id;
    this.userId = userId;
    this.cards = cards;
  }
}
