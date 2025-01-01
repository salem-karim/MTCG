package org.mtcg.utils.battle;

import org.mtcg.models.Card;

public class FireElvesEvadeDragons implements SpecialCase {
  @Override
  public boolean applies(Card card1, Card card2) {
    // FireElves know Dragons and can evade their attacks, so if one card is a
    // FireElf and the other is a Dragon
    return (card1.getName().contains("FireElf") && card2.getName().contains("Dragon")) ||
        (card2.getName().contains("FireElf") && card1.getName().contains("Dragon"));
  }

  @Override
  public Card apply(Card card1, Card card2) {
    // The FireElf evades the Dragon's attack
    return card1.getName().contains("FireElf") ? card1 : card2;
  }
}
