package org.mtcg.utils;

import org.mtcg.models.Card;
import org.mtcg.models.Deck;
import org.mtcg.models.User;
import org.mtcg.db.BattleDbAccess;
import org.mtcg.db.DbConnection;
import org.mtcg.db.DeckDbAccess;
import org.mtcg.db.StackDbAccess;
import org.mtcg.db.UserDbAccess;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class BattleExecutor implements Callable<String> {
  private enum BattleResult {
    PLAYING,
    TIE,
    USER1_WIN,
    USER2_WIN,
  }

  private BattleResult result;
  private final ArrayList<String> battleLog = new ArrayList<>();
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

    return performBattle(deck1, deck2); // Return battle log
  }

  private String performBattle(final Deck deck1, final Deck deck2) throws Exception {
    if (deck1 == null || deck2 == null) {
      throw new NullPointerException("One of the user's decks are null");
    }
    while (result == BattleResult.PLAYING) {
      final var card1 = deck1.getCards()[1];
      final var card2 = deck2.getCards()[1];
      battleLog.add(playRound(card1, card2));
    }

    switch (result) {
      case BattleResult.TIE:
        break;
      case BattleResult.USER1_WIN:
        updateDb(user1, deck1, user2, deck2);
        break;
      case BattleResult.USER2_WIN:
        updateDb(user2, deck2, user1, deck1);
        break;
      default:
        break;
    }
    return "Battle log placeholder";
  }

  private void updateDb(final User winner, final Deck winnerDeck, final User looser, final Deck looserDeck)
      throws SQLException {
    try {
      final var winnerStackId = stackDbAccess.getStackId(DbConnection.getConnection(), winner.getId());
      final var looserStackId = stackDbAccess.getStackId(DbConnection.getConnection(), looser.getId());
      battleDbAccess.updateStacksAndDecks(winner, winnerStackId, winnerDeck, looser, looserStackId, looserDeck);

    } catch (SQLException e) {
      throw e;
    }
  }

  private String playRound(final Card card1, final Card card2) {
    return "Placeholder";
  }
}
