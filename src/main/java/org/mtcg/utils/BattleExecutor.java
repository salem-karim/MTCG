package org.mtcg.utils;

import org.mtcg.models.Deck;
import org.mtcg.models.User;
import org.mtcg.db.BattleDbAccess;
import org.mtcg.db.DbConnection;
import org.mtcg.db.DeckDbAccess;
import org.mtcg.db.StackDbAccess;
import org.mtcg.db.UserDbAccess;

import java.sql.SQLException;
import java.util.concurrent.Callable;

public class BattleExecutor implements Callable<String> {
  private enum BattleResult {
    PLAYING,
    TIE,
    USER1_WIN,
    USER2_WIN,
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
    while (result == BattleResult.PLAYING) {
      battleLog += playRound(deck1, deck2);
    }
    switch (result) {
      case BattleResult.USER1_WIN:
        updateDb(user1, user2, deck2);
        break;
      case BattleResult.USER2_WIN:
        updateDb(user2, user1, deck1);
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

  private String playRound(final Deck deck1, final Deck deck2) {
    String toReturn = "";
    for (int i = 0; i < 5; i++) {
      if (i == 4) {
        user1.setElo(user1.getElo() + 3);
        user1.setWins(user1.getWins() + 1);
        user2.setElo(user2.getElo() - 5);
        user2.setLosses(user2.getLosses() + 1);
        result = BattleResult.USER1_WIN;
        toReturn += "Hello World!";
      }

      toReturn += "Hello World!, ";
    }
    return toReturn;
  }
}
