package org.mtcg.utils.battle;

import org.mtcg.models.Card;

public class KrakenIsImmuneToSpells implements SpecialCase {
  @Override
  public boolean applies(Card card1, Card card2) {
    // The Kraken is immune to spells, so if one of the cards is a Kraken and the
    // other is a spell
    return (card1.getName().contains("Kraken") && card2.getCardType() == Card.CardType.SPELL) ||
        (card2.getName().contains("Kraken") && card1.getCardType() == Card.CardType.SPELL);
  }

  @Override
  public Card apply(Card card1, Card card2) {
    // The Kraken is immune to spells, so the Kraken wins
    return card1.getName().contains("Kraken") ? card1 : card2;
  }
}
