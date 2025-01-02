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
  private static final double TWO_CARD_BOOST = 1.25;
  private static final double ONE_CARD_BOOST = 1.5;

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

  public void applyDamageBoost() {
    int size = cards.size();
    if (size == 2) {
      applyBoostToCards(TWO_CARD_BOOST);
    } else if (size == 1) {
      applyBoostToCards(ONE_CARD_BOOST);
    }
  }

  public void restoreDamage() {
    int size = cards.size();
    if (size <= 2) {
      double currentBoost = (size == 1) ? ONE_CARD_BOOST : TWO_CARD_BOOST;
      for (Card card : cards) {
        card.setDamage(card.getDamage() / currentBoost);
      }
    }
  }

  private void applyBoostToCards(double boostFactor) {
    for (Card card : cards) {
      card.setDamage(card.getDamage() * boostFactor);
    }
  }

  public boolean isSmallDeck() {
    return cards.size() <= 2;
  }

  public double getSmallDeckBoostProbability() {
    return cards.size() == 1 ? 0.5 : cards.size() == 2 ? 0.25 : 0.0;
  }

}
