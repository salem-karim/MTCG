package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import org.mtcg.models.Deck;
import org.mtcg.models.User;
import org.mtcg.models.UserStats;

public class BattleDbAccess {
  private static final Logger logger = Logger.getLogger(BattleDbAccess.class.getName());
  private final StackDbAccess stackDbAccess;
  private final DeckDbAccess deckDbAccess;
  private final UserDbAccess userDbAccess;

  public BattleDbAccess(final StackDbAccess stackDbAccess, final DeckDbAccess deckDbAccess,
      final UserDbAccess userDbAccess) {
    this.stackDbAccess = stackDbAccess;
    this.deckDbAccess = deckDbAccess;
    this.userDbAccess = userDbAccess;
  }

  public void updateStacksAndDecks(final User winner, final UUID winnerStackId,
      final User looser, final UUID looserStackId,
      final Deck looserDeck)
      throws SQLException {
    try (final var connection = DbConnection.getConnection()) {
      try {
        connection.setAutoCommit(false);

        // Remove loosers deck from his stack and Deck
        deckDbAccess.deleteDeckCards(connection, looserDeck.getId());

        int index = 0;
        final var loosersDecksCardIds = new UUID[4];
        for (final var card : looserDeck.getCards()) {
          loosersDecksCardIds[index] = card.getId();
          index++;
        }

        // Insert the loosers Deck Cards into the winner Stack
        stackDbAccess.moveCardsToOtherStack(connection, winnerStackId, looserStackId, loosersDecksCardIds);

        // Update the users stats
        userDbAccess.updateUserStats(connection,
            new UserStats(winner.getUsername(), winner.getElo(), winner.getWins(), winner.getLosses()));
        userDbAccess.updateUserStats(connection,
            new UserStats(looser.getUsername(), looser.getElo(), looser.getWins(), looser.getLosses()));

        connection.commit();

      } catch (final SQLException e) {
        logger.severe("Failed to update Cards and Stats: " + e.getMessage());
        handleRollback(connection);
      } catch (final Exception e) {
        logger.severe("Failed to update Cards and Stats: " + e.getMessage());
      }
    }
  }

  private void handleRollback(final Connection con) {
    try {
      if (!con.getAutoCommit()) {
        con.rollback();
        logger.info("Transaction rolled back due to failure.");
      }
    } catch (final SQLException rollbackEx) {
      logger.severe("Rollback failed: " + rollbackEx.getMessage());
    }
  }

}
