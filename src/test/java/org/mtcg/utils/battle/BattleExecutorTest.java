package org.mtcg.utils.battle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtcg.models.Card;
import org.mtcg.models.Deck;

class BattleExecutorTest {
  private BattleExecutor battleExecutor;

  @BeforeEach
  void setUp() {
    battleExecutor = new BattleExecutor(null, null);
  }

  @Test
  void testFight_PureMonsterFight() {
    Card strongerMonster = new Card(
        UUID.randomUUID(),
        "StrongMonster",
        100.0,
        Card.CardType.MONSTER,
        Card.Element.NORMAL);

    Card weakerMonster = new Card(
        UUID.randomUUID(),
        "WeakMonster",
        50.0,
        Card.CardType.MONSTER,
        Card.Element.NORMAL);

    var result = battleExecutor.fight(strongerMonster, weakerMonster);
    assertEquals(weakerMonster, result.first); // Loser
    assertEquals(strongerMonster, result.second); // Winner
  }

  @Test
  void testMoveLooserCardIntoWinnerDeck() {
    // Create 4 cards for each deck
    List<Card> deck1Cards = new ArrayList<>();
    List<Card> deck2Cards = new ArrayList<>();

    // Create cards for deck1
    for (int i = 0; i < 4; i++) {
      deck1Cards.add(new Card(
          UUID.randomUUID(),
          "Card1_" + i,
          50.0 + i,
          Card.CardType.MONSTER,
          Card.Element.NORMAL));
    }

    // Create cards for deck2
    for (int i = 0; i < 4; i++) {
      deck2Cards.add(new Card(
          UUID.randomUUID(),
          "Card2_" + i,
          60.0 + i,
          Card.CardType.MONSTER,
          Card.Element.NORMAL));
    }

    // Create decks with IDs
    Deck deck1 = new Deck(deck1Cards, UUID.randomUUID());
    Deck deck2 = new Deck(deck2Cards, UUID.randomUUID());

    // Pick a card to move (let's use the first card from deck1)
    Card cardToMove = deck1Cards.get(0);

    // Test moving the card
    battleExecutor.moveLooserCardIntoWinnerDeck(deck1, deck2, cardToMove);

    // Verify the move
    assertFalse(deck1.getCards().contains(cardToMove), "Deck1 should no longer contain the moved card");
    assertTrue(deck2.getCards().contains(cardToMove), "Deck2 should now contain the moved card");
    assertEquals(3, deck1.getCards().size(), "Deck1 should have 3 cards");
    assertEquals(5, deck2.getCards().size(), "Deck2 should have 5 cards");
  }

  @Test
  void testFight_DragonVsGoblin_SpecialCase() {
    Card dragon = new Card(
        UUID.randomUUID(),
        "Dragon",
        50.0,
        Card.CardType.MONSTER,
        Card.Element.NORMAL);

    Card goblin = new Card(
        UUID.randomUUID(),
        "Goblin",
        100.0,
        Card.CardType.MONSTER,
        Card.Element.NORMAL);

    var result = battleExecutor.fight(goblin, dragon);
    assertEquals(goblin, result.first); // Goblin should lose
    assertEquals(dragon, result.second); // Dragon should win
  }

  @Test
  void testFight_SpellFight() {
    Card waterSpell = new Card(
        UUID.randomUUID(),
        "WaterSpell",
        50.0,
        Card.CardType.SPELL,
        Card.Element.WATER);

    Card fireSpell = new Card(
        UUID.randomUUID(),
        "FireSpell",
        50.0,
        Card.CardType.SPELL,
        Card.Element.FIRE);

    var result = battleExecutor.fight(fireSpell, waterSpell);
    assertEquals(fireSpell, result.first); // Fire loses to Water
    assertEquals(waterSpell, result.second); // Water wins against Fire
  }
}
