package org.mtcg.utils.battle;

import org.mtcg.models.Card;

public interface SpecialCase {
  // Check if this special case applies to the given cards
  boolean applies(Card card1, Card card2);

  // Return the winning card for this special case
  Card apply(Card card1, Card card2);
}
