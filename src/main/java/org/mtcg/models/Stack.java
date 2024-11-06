package org.mtcg.models;

import java.util.ArrayList;
import java.util.List;

public class Stack {
  private final List<Card> cards;

  public Stack() {
    cards = new ArrayList<>();
  }

  public void addCard(final Card card) {
    cards.add(card);
  }

  public void removeCard(final Card card) {
    cards.remove(card);
  }
}
