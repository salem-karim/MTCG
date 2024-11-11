package org.mtcg.models;

import java.util.List;
import java.util.UUID;

public class Stack {
  private final UUID id;
  private final UUID userId;
  private final List<Card> cards;

  // Constructor for DB
  public Stack(UUID id, UUID userId, List<Card> cards) {
    this.id = id;
    this.userId = userId;
    this.cards = cards;
  }

  public void addCard(final Card card) {
    cards.add(card);
  }

  public void removeCard(final Card card) {
    cards.remove(card);
  }
}
