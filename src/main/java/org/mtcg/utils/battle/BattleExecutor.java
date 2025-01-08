package org.mtcg.utils.battle;

import static org.mtcg.utils.battle.ElementEffect.calculateDamage;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import org.mtcg.db.BattleDbAccess;
import org.mtcg.db.DbConnection;
import org.mtcg.db.DeckDbAccess;
import org.mtcg.db.StackDbAccess;
import org.mtcg.db.UserDbAccess;
import org.mtcg.models.Card;
import org.mtcg.models.Deck;
import org.mtcg.models.User;
import org.mtcg.utils.Pair;

public class BattleExecutor implements Callable<String> {
  private enum BattleResult {
    PLAYING,
    TIE,
    USER1_WIN,
    USER2_WIN
  }

  private BattleResult result = BattleResult.PLAYING;
  private String battleLog = "";
  private final DeckDbAccess deckDbAccess = new DeckDbAccess();
  private final StackDbAccess stackDbAccess = new StackDbAccess();
  private final BattleDbAccess battleDbAccess = new BattleDbAccess(stackDbAccess, deckDbAccess, new UserDbAccess());
  private final User user1;
  private final User user2;

  public BattleExecutor(final User user1, final User user2) {
    this.user1 = user1;
    this.user2 = user2;
  }

  @Override
  public String call() throws Exception {
    // Battle logic here
    final var deck1 = deckDbAccess.getDeckCards(deckDbAccess.getDeckId(user1.getId()));
    final var deck2 = deckDbAccess.getDeckCards(deckDbAccess.getDeckId(user2.getId()));
    if (deck1 == null || deck2 == null) {
      throw new NullPointerException("One of the user's decks are null");
    }
    return performBattle(deck1, deck2); // Return battle log
  }

  protected String performBattle(final Deck deck1, final Deck deck2) throws Exception {
    // Make copy of Deck using Copy Constructor
    final var battleDeck1 = new Deck(deck1);
    final var battleDeck2 = new Deck(deck2);
    int rounds = 1;
    System.out.println(rounds);
    while (rounds <= 100 && result == BattleResult.PLAYING) {
      battleLog += playRound(battleDeck1, battleDeck2, rounds);
      rounds++;
    }

    // If rounds was 100 make the result a Tie
    if (rounds >= 100) {
      result = BattleResult.TIE;
    }

    // update DB when result was not a Tie
    switch (result) {
      case BattleResult.USER1_WIN:
        updateDb(user1, user2, deck2);
        break;
      case BattleResult.USER2_WIN:
        updateDb(user2, user1, deck1);
        break;
      case BattleResult.TIE:
        battleLog += "Battle ended in a Tie.\n";
        break;
      default:
        break;
    }
    // return the battle Log as one String
    return battleLog;
  }

  private void updateDb(final User winner, final User looser, final Deck looserDeck)
      throws SQLException {
    // get both StackIds and change the stackIds in the stack_cards table
    // to the winners StackIds
    // also Delete the cards of the looser from the deck_cards table
    final var winnerStackId = stackDbAccess.getStackId(DbConnection.getConnection(), winner.getId());
    final var looserStackId = stackDbAccess.getStackId(DbConnection.getConnection(), looser.getId());
    battleDbAccess.updateStacksAndDecks(winner, winnerStackId, looser, looserStackId, looserDeck);

  }

  private String playRound(final Deck deck1, final Deck deck2, final int round) {
    // If either of the decks are empty set the needed Data
    if (deck1.getCards().isEmpty()) {
      result = BattleResult.USER2_WIN;
      user2.setElo(user2.getElo() + 3);
      user2.setWins(user2.getWins() + 1);
      user1.setElo(user1.getElo() - 5);
      user1.setLosses(user1.getLosses() + 1);
      return "User " + user2.getUsername() + " won the Battle.";
    } else if (deck2.getCards().isEmpty()) {
      result = BattleResult.USER1_WIN;
      user1.setElo(user1.getElo() + 3);
      user1.setWins(user1.getWins() + 1);
      user2.setElo(user2.getElo() - 5);
      user2.setLosses(user2.getLosses() + 1);
      return "User " + user1.getUsername() + " won the Battle.";
    } else {
      // else apply the Boost if possible, fight restore Damage because of Math
      // reasons and handle non tie outcome
      deck1.applyDamageBoost();
      deck2.applyDamageBoost();
      final Pair<Card> resultPair = fight(deck1, deck1.getRandomCard(), deck2, deck2.getRandomCard());
      deck1.restoreDamage();
      deck2.restoreDamage();
      if (resultPair != null) {
        // if there is a winner move the card from looser deck to winner deck which are
        // copies so the original are the same for DB Reasons
        final var looserCard = resultPair.first;
        final var winnerCard = resultPair.second;
        moveLooserCardIntoWinnerDeck(deck1, deck2, looserCard);

        // Determine which user won and get their username
        String winnerUsername = deck1.getCards().contains(winnerCard) ? user1.getUsername() : user2.getUsername();
        String loserUsername = deck2.getCards().contains(looserCard) ? user1.getUsername() : user2.getUsername();

        return "Round " + round + ": User " + winnerUsername + "'s Card: " + winnerCard.getName() +
            " won against User " + loserUsername + "'s Card: " + looserCard.getName() + ".\n";
      } else {
        return "Round " + round + ": The round ended in a Tie.\n";
      }
    }
  }

  protected void moveLooserCardIntoWinnerDeck(Deck deck1, Deck deck2, Card looserCard) {
    // Restore any damage boosts before moving
    deck1.restoreDamage();
    deck2.restoreDamage();

    // Move the card needed to Override equals method in Card class
    if (deck1.getCards().contains(looserCard)) {
      deck1.getCards().remove(looserCard);
      deck2.getCards().add(looserCard);
    } else if (deck2.getCards().contains(looserCard)) {
      deck2.getCards().remove(looserCard);
      deck1.getCards().add(looserCard);
    }

    // Reapply appropriate boosts after moving
    deck1.applyDamageBoost();
    deck2.applyDamageBoost();
  }

  protected Pair<Card> fight(final Deck battleDeck1, final Card card1, final Deck battleDeck2, final Card card2) {
    Deck deck1Contains = battleDeck1.getCards().contains(card1) ? battleDeck1 : battleDeck2;
    Deck deck2Contains = battleDeck2.getCards().contains(card2) ? battleDeck2 : battleDeck1;

    // Check for special cases first
    List<SpecialCase> specialCases = List.of(
        new GoblinsAreAfraidOfDragons(),
        new WizardsControlOrks(),
        new KnightsVsWaterSpells(),
        new KrakenIsImmuneToSpells(),
        new FireElvesEvadeDragons());

    // Handle special cases with small deck probability reversal
    for (SpecialCase specialCase : specialCases) {
      if (specialCase.applies(card1, card2)) {
        Card normalWinner = specialCase.apply(card1, card2);

        // Check if the losing deck is small and should get a chance to reverse the
        // outcome
        Deck losingDeck = (normalWinner == card1) ? deck2Contains : deck1Contains;
        if (losingDeck.isSmallDeck()) {
          double reverseProbability = losingDeck.getSmallDeckBoostProbability();
          if (Math.random() < reverseProbability) {
            // Reverse the outcome
            Card actualWinner = (normalWinner == card1) ? card2 : card1;
            actualWinner.tryApplyVictoryBoost();
            return new Pair<>(normalWinner, actualWinner); // Reversed!
          }
        }

        normalWinner.tryApplyVictoryBoost();
        return new Pair<>(normalWinner == card1 ? card2 : card1, normalWinner);
      }
    }

    // Normal combat resolution
    if (card1.getCardType() == Card.CardType.MONSTER && card2.getCardType() == Card.CardType.MONSTER) {
      // use Math.abs to get the absolute difference and
      // check if the double precision error is smaller then 0.001
      if (Math.abs(card1.getDamage() - card2.getDamage()) < 0.001)
        return null;
      Card winner = card1.getDamage() > card2.getDamage() ? card1 : card2;
      winner.tryApplyVictoryBoost();
      return new Pair<>(winner == card1 ? card2 : card1, winner);
    }

    // Spell combat resolution
    if (card1.getCardType() == Card.CardType.SPELL || card2.getCardType() == Card.CardType.SPELL) {

      double damage1 = card1.getDamage() * calculateDamage(card1, card2);
      double damage2 = card2.getDamage() * calculateDamage(card2, card1);

      // the same check here
      if (Math.abs(damage1 - damage2) < 0.001)
        return null; // Tie case
      Card winner = damage1 > damage2 ? card1 : card2;
      winner.tryApplyVictoryBoost();
      return new Pair<>(winner == card1 ? card2 : card1, winner);
    }

    return null; // Tie case
  }

}
