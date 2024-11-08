package org.mtcg.models;

import java.util.UUID;

import lombok.Getter;

@Getter
public class Package {
  private static final int PACKAGE_SIZE = 5;
  private final Card[] cards;
  private UUID id;
  private UUID userId;

  public Package(final Card[] cards) {
    if (cards.length != PACKAGE_SIZE)
      throw new IllegalArgumentException("A package must contain exactly " + PACKAGE_SIZE + " cards.");
    this.cards = cards;
  }
}
