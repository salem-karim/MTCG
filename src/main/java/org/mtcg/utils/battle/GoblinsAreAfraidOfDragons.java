package org.mtcg.utils.battle;

import org.mtcg.models.Card;

public class GoblinsAreAfraidOfDragons implements SpecialCase {
  @Override
  public boolean applies(Card card1, Card card2) {
    return (card1.getName().contains("Goblin") && card2.getName().contains("Dragon")) ||
        (card2.getName().contains("Goblin") && card1.getName().contains("Dragon"));
  }

  @Override
  public Card apply(Card card1, Card card2) {
    // Goblins are afraid of Dragons, so the Dragon wins
    return card1.getName().contains("Goblin") ? card2 : card1;
  }
}
