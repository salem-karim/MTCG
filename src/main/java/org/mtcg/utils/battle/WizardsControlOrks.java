package org.mtcg.utils.battle;

import org.mtcg.models.Card;

public class WizardsControlOrks implements SpecialCase {
  @Override
  public boolean applies(Card card1, Card card2) {
    return (card1.getName().contains("Wizard") && card2.getName().contains("Ork")) ||
        (card2.getName().contains("Wizard") && card1.getName().contains("Ork"));
  }

  @Override
  public Card apply(Card card1, Card card2) {
    // Wizards control Orks, so the Wizard wins
    return card1.getName().contains("Wizard") ? card1 : card2;
  }
}
