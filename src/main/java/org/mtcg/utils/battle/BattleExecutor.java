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

  private String performBattle(final Deck deck1, final Deck deck2) throws Exception {
    final var battleDeck1 = new Deck(deck1);
    final var battleDeck2 = new Deck(deck2);
    int rounds = 1;
    System.out.println(rounds);
    while (rounds <= 100 && result == BattleResult.PLAYING) {
      battleLog += playRound(battleDeck1, battleDeck2, rounds);
      rounds++;
    }

    if (rounds >= 100) {
      result = BattleResult.TIE;
    }

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
    return battleLog;
  }

  private void updateDb(final User winner, final User looser, final Deck looserDeck)
      throws SQLException {
    final var winnerStackId = stackDbAccess.getStackId(DbConnection.getConnection(), winner.getId());
    final var looserStackId = stackDbAccess.getStackId(DbConnection.getConnection(), looser.getId());
    battleDbAccess.updateStacksAndDecks(winner, winnerStackId, looser, looserStackId, looserDeck);

  }

  private String playRound(final Deck deck1, final Deck deck2, final int round) {
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
      final Pair<Card> resultPair = fight(deck1.getRandomCard(), deck2.getRandomCard());
      if (resultPair != null) {
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
    // The winner is the deck that does not contain the looser card
    if (deck1.getCards().contains(looserCard)) {
      deck1.getCards().remove(looserCard);
      deck2.getCards().add(looserCard);
    } else if (deck2.getCards().contains(looserCard)) {
      deck2.getCards().remove(looserCard);
      deck1.getCards().add(looserCard);
    }
  }

  protected Pair<Card> fight(final Card card1, final Card card2) {
    List<SpecialCase> specialCases = List.of(
        new GoblinsAreAfraidOfDragons(),
        new WizardsControlOrks(),
        new KnightsVsWaterSpells(),
        new KrakenIsImmuneToSpells(),
        new FireElvesEvadeDragons());

    // Step 1: Check for any special case
    for (SpecialCase specialCase : specialCases) {
      if (specialCase.applies(card1, card2)) {
        // If a special case applies, apply the logic and return the result
        Card winner = specialCase.apply(card1, card2);
        return new Pair<>(winner == card1 ? card2 : card1, winner);
      }
    }

    // Step 2: Handle Pure Monster Fights
    if (card1.getCardType() == Card.CardType.MONSTER && card2.getCardType() == Card.CardType.MONSTER) {
      return card1.getDamage() > card2.getDamage() ? new Pair<>(card2, card1) : new Pair<>(card1, card2);
    }

    // Step 3: Handle Spell Fights
    if (card1.getCardType() == Card.CardType.SPELL || card2.getCardType() == Card.CardType.SPELL) {
      double damage1 = card1.getDamage() * calculateDamage(card1, card2);
      double damage2 = card2.getDamage() * calculateDamage(card2, card1);

      return damage1 > damage2 ? new Pair<>(card2, card1) : new Pair<>(card1, card2);
    }

    return null;
  }

}
