package org.mtcg.models;

import lombok.Getter;

@Getter
public class Package {
  private static final int PACKAGE_SIZE = 5;
  private final Card[] cards;

  public Package(Card[] cards) {
    if (cards.length != PACKAGE_SIZE)
      throw new IllegalArgumentException("A package must contain exactly " + PACKAGE_SIZE + " cards.");
    this.cards = cards;
  }
}
