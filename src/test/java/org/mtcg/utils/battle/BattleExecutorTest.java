package org.mtcg.utils.battle;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtcg.models.Card;
import org.mtcg.models.Deck;
import org.mtcg.utils.Pair;

class BattleExecutorTest {
  private BattleExecutor battleExecutor;

  @BeforeEach
  void setUp() {
    battleExecutor = new BattleExecutor(null, null);
  }

  @Test
  void testFight_PureMonsterFight() {
    // Create decks
    List<Card> strongerDeckCards = new ArrayList<>();
    List<Card> weakerDeckCards = new ArrayList<>();

    for (int i = 0; i < 4; i++) {
      strongerDeckCards.add(new Card(
          UUID.randomUUID(),
          "StrongMonster",
          100.0,
          Card.CardType.MONSTER,
          Card.Element.NORMAL));
    }

    for (int i = 0; i < 4; i++) {
      weakerDeckCards.add(new Card(
          UUID.randomUUID(),
          "WeakMonster",
          50.0,
          Card.CardType.MONSTER,
          Card.Element.NORMAL));
    }

    Deck strongerDeck = new Deck(strongerDeckCards, UUID.randomUUID());
    Deck weakerDeck = new Deck(weakerDeckCards, UUID.randomUUID());

    Card weakerMonster = weakerDeck.getRandomCard();
    Card strongerMonster = strongerDeck.getRandomCard();

    Pair<Card> result = battleExecutor.fight(strongerDeck, strongerMonster, weakerDeck, weakerMonster);
    assertEquals(weakerMonster, result.first); // Loser
    assertEquals(strongerMonster, result.second); // Winner
  }

  @Test
  void testMoveLooserCardIntoWinnerDeck() {
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

    Deck deck1 = new Deck(deck1Cards, UUID.randomUUID());
    Deck deck2 = new Deck(deck2Cards, UUID.randomUUID());
    Card cardToMove = deck1Cards.get(0);

    battleExecutor.moveLooserCardIntoWinnerDeck(deck1, deck2, cardToMove);

    assertFalse(deck1.getCards().contains(cardToMove));
    assertTrue(deck2.getCards().contains(cardToMove));
    assertEquals(3, deck1.getCards().size());
    assertEquals(5, deck2.getCards().size());

    // Check that the card's damage was properly restored (no boost)
    assertEquals(50.0, cardToMove.getDamage(), 0.001);
  }

  @Test
  void testFight_DragonVsGoblin_SpecialCase() {
    List<Card> dragonDeckCards = new ArrayList<>();
    List<Card> goblinDeckCards = new ArrayList<>();

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

    // Fill the decks
    dragonDeckCards.add(dragon);
    goblinDeckCards.add(goblin);
    for (int i = 0; i < 3; i++) {
      dragonDeckCards.add(new Card(
          UUID.randomUUID(),
          "Filler",
          50.0,
          Card.CardType.MONSTER,
          Card.Element.NORMAL));
      goblinDeckCards.add(new Card(
          UUID.randomUUID(),
          "Filler",
          50.0,
          Card.CardType.MONSTER,
          Card.Element.NORMAL));
    }

    Deck dragonDeck = new Deck(dragonDeckCards, UUID.randomUUID());
    Deck goblinDeck = new Deck(goblinDeckCards, UUID.randomUUID());

    Pair<Card> result = battleExecutor.fight(goblinDeck, goblin, dragonDeck, dragon);
    assertEquals(goblin, result.first); // Goblin should lose
    assertEquals(dragon, result.second); // Dragon should win
  }

  @Test
  void testFight_SpellFight() {
    List<Card> waterDeckCards = new ArrayList<>();
    List<Card> fireDeckCards = new ArrayList<>();

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

    // Fill the decks
    waterDeckCards.add(waterSpell);
    fireDeckCards.add(fireSpell);
    for (int i = 0; i < 3; i++) {
      waterDeckCards.add(new Card(
          UUID.randomUUID(),
          "Filler",
          50.0,
          Card.CardType.SPELL,
          Card.Element.WATER));
      fireDeckCards.add(new Card(
          UUID.randomUUID(),
          "Filler",
          50.0,
          Card.CardType.SPELL,
          Card.Element.FIRE));
    }

    Deck waterDeck = new Deck(waterDeckCards, UUID.randomUUID());
    Deck fireDeck = new Deck(fireDeckCards, UUID.randomUUID());

    Pair<Card> result = battleExecutor.fight(fireDeck, fireSpell, waterDeck, waterSpell);
    assertEquals(fireSpell, result.first); // Fire loses to Water
    assertEquals(waterSpell, result.second); // Water wins against Fire
  }

  // New tests for deck size boost mechanics
  @Test
  void testDeckSizeBoost() {
    List<Card> deckCards = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      deckCards.add(new Card(
          UUID.randomUUID(),
          "Card_" + i,
          100.0,
          Card.CardType.MONSTER,
          Card.Element.NORMAL));
    }

    Deck deck = new Deck(deckCards, UUID.randomUUID());

    // Test initial state (4 cards)
    deck.applyDamageBoost();
    assertEquals(100.0, deck.getCards().get(0).getDamage(), 0.001);

    // Remove two cards to test 25% boost
    deck.getCards().remove(0);
    deck.getCards().remove(0);
    deck.applyDamageBoost();
    assertEquals(125.0, deck.getCards().get(0).getDamage(), 0.001);

    // Restore damage
    deck.restoreDamage();
    assertEquals(100.0, deck.getCards().get(0).getDamage(), 0.001);
  }

  @Test
  void testVictoryBoost() {
    Card winner = new Card(
        UUID.randomUUID(),
        "Winner",
        100.0,
        Card.CardType.MONSTER,
        Card.Element.NORMAL);

    // Force multiple victories to verify boost is only applied once
    for (int i = 0; i < 5; i++) {
      winner.tryApplyVictoryBoost();
    }

    // The damage should either be 100 (no boost) or 130 (boosted once)
    double finalDamage = winner.getDamage();
    assertTrue(finalDamage == 100.0 || finalDamage == 130.0);

    // If boosted, verify it can't be boosted again
    if (winner.isHasVictoryBoost()) {
      double damageBeforeAttempt = winner.getDamage();
      winner.tryApplyVictoryBoost();
      assertEquals(damageBeforeAttempt, winner.getDamage(), 0.001);
    }
  }

  @Test
  void testSmallDeckSpecialCaseReversal() {
    List<Card> goblinDeckCards = new ArrayList<>();
    List<Card> dragonDeckCards = new ArrayList<>();

    // Main cards for testing
    Card goblin = new Card(
        UUID.randomUUID(),
        "Goblin",
        100.0,
        Card.CardType.MONSTER,
        Card.Element.NORMAL);
    Card dragon = new Card(
        UUID.randomUUID(),
        "Dragon",
        150.0,
        Card.CardType.MONSTER,
        Card.Element.NORMAL);

    // Create full goblin deck first (4 cards)
    goblinDeckCards.add(goblin);
    for (int i = 0; i < 3; i++) {
      goblinDeckCards.add(new Card(
          UUID.randomUUID(),
          "GoblinFiller_" + i,
          50.0,
          Card.CardType.MONSTER,
          Card.Element.NORMAL));
    }

    // Create full dragon deck (4 cards)
    dragonDeckCards.add(dragon);
    for (int i = 0; i < 3; i++) {
      dragonDeckCards.add(new Card(
          UUID.randomUUID(),
          "DragonFiller_" + i,
          50.0,
          Card.CardType.MONSTER,
          Card.Element.NORMAL));
    }

    Deck goblinDeck = new Deck(goblinDeckCards, UUID.randomUUID());
    Deck dragonDeck = new Deck(dragonDeckCards, UUID.randomUUID());

    // Remove cards from goblin deck to test small deck mechanics
    // Remove 2 cards to leave only 2 cards in the deck
    goblinDeck.getCards().remove(3);
    goblinDeck.getCards().remove(2);

    // Run multiple fights to test probability
    int reversalsTwoCards = 0;
    int totalFights = 1000;

    for (int i = 0; i < totalFights; i++) {
      Pair<Card> result = battleExecutor.fight(goblinDeck, goblin, dragonDeck, dragon);
      if (result.second == goblin) {
        reversalsTwoCards++;
      }
    }

    goblinDeck.getCards().remove(1);
    int reversalsOneCard = 0;

    for (int i = 0; i < totalFights; i++) {
      Pair<Card> result = battleExecutor.fight(goblinDeck, goblin, dragonDeck, dragon);
      if (result.second == goblin) {
        reversalsOneCard++;
      }
    }

    // Calculate rates
    double twoCardRate = (double) reversalsTwoCards / totalFights;
    double oneCardRate = (double) reversalsOneCard / totalFights;

    // Assert both probabilities
    assertTrue(twoCardRate > 0.2 && twoCardRate < 0.3,
        "Two-card reversal rate should be approximately 25% but was " + (twoCardRate * 100) + "%");
    assertTrue(oneCardRate > 0.45 && oneCardRate < 0.55,
        "One-card reversal rate should be approximately 50% but was " + (oneCardRate * 100) + "%");
  }
}
