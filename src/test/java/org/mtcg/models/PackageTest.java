package org.mtcg.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtcg.models.Card.CardType;
import org.mtcg.models.Card.Element;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import java.util.stream.Stream;

public class PackageTest {
  private Card[] cards;

  @BeforeEach
  public void setUp() {
    cards = createCards();
  }

  public Card[] createCards() {
    return new Card[] {
        new Card(UUID.randomUUID(), "Ork", 50.0f, CardType.MONSTER, Element.NORMAL),
        new Card(UUID.randomUUID(), "Water Spell", 30.5f, CardType.SPELL, Element.WATER),
        new Card(UUID.randomUUID(), "Knight", 40.0f, CardType.MONSTER, Element.NORMAL),
        new Card(UUID.randomUUID(), "Fire Dragon", 45.0f, CardType.MONSTER, Element.FIRE),
    };
  }

  @Test
  public void tooLittleCards() {
    // Test for less than 5 cards (should throw exception)
    assertThrows(IllegalArgumentException.class, () -> {
      new Package(cards, UUID.randomUUID());
    });
  }

  @Test
  public void tooManyCards() {
    // Add two extra cards to exceed the limit
    final Card[] extraCards = {
        new Card(UUID.randomUUID(), "FireOrk", 45.0f, CardType.MONSTER, Element.FIRE),
        new Card(UUID.randomUUID(), "KnifeSpell", 35.0f, CardType.SPELL, Element.NORMAL),
    };

    // Combine cards and extraCards into a single array with more than 5 cards
    final Card[] tooManyCards = Stream.concat(Stream.of(cards), Stream.of(extraCards))
        .toArray(Card[]::new);

    // Test for more than 5 cards (should throw exception)
    assertThrows(IllegalArgumentException.class, () -> {
      new Package(tooManyCards, UUID.randomUUID());
    });
  }

  @Test
  public void exactPackageSize() {
    // Add one extra card to make it exactly 5 cards
    final Card extraCard = new Card(UUID.randomUUID(), "Extra Card", 25.0f, CardType.MONSTER, Element.NORMAL);

    final Card[] exactCards = Stream.concat(Stream.of(cards), Stream.of(extraCard))
        .toArray(Card[]::new);

    // Test for exactly 5 cards (should not throw exception and return a valid
    // Package object)
    final Package pkg = new Package(exactCards, UUID.randomUUID());
    assertNotNull(pkg, "Package should be created without exceptions when exactly 5 cards are provided");
  }
}
