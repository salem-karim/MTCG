package org.mtcg.models;

import java.util.ArrayList;
import java.util.UUID;

import lombok.Getter;

@Getter
public class Deck {
  private static final int DECK_SIZE = 4;
  private final ArrayList<Card> cards;
  private final UUID id;

  public Deck(final ArrayList<Card> cards, final UUID id) {
    this.id = id;
    if (cards.size() != DECK_SIZE)
      throw new IllegalArgumentException("A package must contain exactly " + DECK_SIZE + " cards.");
    this.cards = cards;
  }

  public Deck(final Deck copy) {
    this.cards = copy.getCards();
    this.id = copy.getId();
  }

  public Card getRandomCard() {
    return cards.get((int) (Math.random() * cards.size()));
  }
}
