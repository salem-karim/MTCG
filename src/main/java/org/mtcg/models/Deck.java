package org.mtcg.models;

import java.util.List;
import java.util.LinkedList;
import java.util.UUID;

import lombok.Getter;

@Getter
public class Deck {
  private static final int DECK_SIZE = 4;
  private final List<Card> cards;
  private final UUID id;

  public Deck(final List<Card> cards, final UUID id) {
    this.id = id;
    if (cards.size() != DECK_SIZE)
      throw new IllegalArgumentException("A package must contain exactly " + DECK_SIZE + " cards.");
    this.cards = new LinkedList<>(cards); // Create a new list to ensure a deep copy
  }

  public Deck(final Deck copy) {
    this.id = copy.getId();
    this.cards = new LinkedList<>(copy.getCards()); // Create a new list to ensure a deep copy
  }

  public Card getRandomCard() {
    return cards.get((int) (Math.random() * cards.size()));
  }
}
