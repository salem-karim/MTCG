package org.mtcg.models;

import java.util.ArrayList;
import java.util.List;

public class Stack {
  private final List<Card> cards;

  public Stack() {
    cards = new ArrayList<>();
  }
  public void addCard(Card card) {
    cards.add(card);
  }
  public void removeCard(Card card) {
    cards.remove(card);
  }
}
