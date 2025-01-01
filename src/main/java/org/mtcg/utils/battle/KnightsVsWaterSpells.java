package org.mtcg.utils.battle;

import org.mtcg.models.Card;

public class KnightsVsWaterSpells implements SpecialCase {
  @Override
  public boolean applies(Card card1, Card card2) {
    // Knights are affected by WaterSpells, but only if one of the cards is a Knight
    // and the other is a WaterSpell
    return (card1.getName().contains("Knight") && card2.getName().contains("WaterSpell")) ||
        (card2.getName().contains("Knight") && card1.getName().contains("WaterSpell"));
  }

  @Override
  public Card apply(Card card1, Card card2) {
    // The Knight is drowned by the WaterSpell instantly
    return card1.getName().contains("Knight") ? card2 : card1;
  }
}
