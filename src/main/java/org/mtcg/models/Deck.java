package org.mtcg.models;

public class Deck {
  private static final int PACKAGE_SIZE = 4;
  private final Card[] cards;

  public Deck(Card[] cards) {
    if (cards.length != PACKAGE_SIZE)
      throw new IllegalArgumentException("A package must contain exactly " + PACKAGE_SIZE + " cards.");
    this.cards = cards;
  }
  public Card getRandomCard(){
    return cards[(int) (Math.random() * cards.length)];
  }
}
