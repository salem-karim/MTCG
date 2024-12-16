package org.mtcg.models;

import java.util.UUID;

import lombok.Getter;

@Getter
public class Deck {
  private static final int DECK_SIZE = 4;
  private final Card[] cards;
  private final UUID id;

  public Deck(final Card[] cards, final UUID id) {
    this.id = id;
    if (cards.length != DECK_SIZE)
      throw new IllegalArgumentException("A package must contain exactly " + DECK_SIZE + " cards.");
    this.cards = cards;
  }

  public Card getRandomCard() {
    return cards[(int) (Math.random() * cards.length)];
  }
}
