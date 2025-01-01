package org.mtcg.utils.battle;

import org.mtcg.models.Card;

public class ElementEffect {

  public static double calculateDamage(Card card1, Card card2) {
    // Water -> Fire
    if (card1.getElement() == Card.Element.WATER && card2.getElement() == Card.Element.FIRE) {
      return 2.0;
    }
    // Fire -> Normal
    else if (card1.getElement() == Card.Element.FIRE && card2.getElement() == Card.Element.NORMAL) {
      return 2.0;
    }
    // Normal -> Water
    else if (card1.getElement() == Card.Element.NORMAL && card2.getElement() == Card.Element.WATER) {
      return 2.0;
    }
    // Fire -> Water
    else if (card1.getElement() == Card.Element.FIRE && card2.getElement() == Card.Element.WATER) {
      return 0.5;
    }
    // Water -> Normal
    else if (card1.getElement() == Card.Element.WATER && card2.getElement() == Card.Element.NORMAL) {
      return 0.5;
    }
    // No effect
    else {
      return 1.0;
    }
  }
}
