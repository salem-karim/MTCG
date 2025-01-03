package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mtcg.models.Card;
import org.mtcg.models.Deck;

public class DeckDbAccess {
  private static final Logger logger = Logger.getLogger(DeckDbAccess.class.getName());

  public UUID getDeckId(final UUID id) {
    try (final var connection = DbConnection.getConnection()) {
      final String IdSQL = "SELECT id FROM decks WHERE user_id = ?";

      try (final var IdStmt = connection.prepareStatement(IdSQL)) {
        IdStmt.setObject(1, id);
        try (final var result = IdStmt.executeQuery()) {
          if (result.next()) {
            return (UUID) result.getObject("id");
          } else {
            logger.warning("Deck of User with ID: " + id + "not found\n");
          }
        }
      }
    } catch (final SQLException e) {
      logger.log(Level.SEVERE, "SQL error while retrieving users deck with ID: " + id, e);
    }
    return null;
  }

  public Deck getDeckCards(final UUID deckId) {
    int index = 0;
    final int DECK_SIZE = 4;
    final var cards = new Card[DECK_SIZE];
    try (final var connection = DbConnection.getConnection()) {
      final String deckCardsSQL = "SELECT * FROM cards " +
          "INNER JOIN deck_cards ON cards.id = deck_cards.card_id " +
          "WHERE deck_cards.deck_id = ?";

      try (final var stmt = connection.prepareStatement(deckCardsSQL)) {
        stmt.setObject(1, deckId);
        try (final var result = stmt.executeQuery()) {
          while (result.next()) {
            cards[index] = new Card(
                (UUID) result.getObject("id"),
                result.getString("name"),
                result.getDouble("damage"));
            index++;
          }
        }
      }
    } catch (final SQLException e) {
      logger.log(Level.SEVERE, "SQL error while retrieving users decks Cards with Deck ID: " + deckId, e);
    }
    if (index == 0) {
      logger.log(Level.INFO, "No cards found for Deck ID: " + deckId);
      return null;
    }
    try {
      return new Deck(new LinkedList<>(Arrays.asList(cards)), deckId);
    } catch (final IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  public boolean configureDeck(final UUID deckId, final UUID[] cardIds, final UUID userId) throws SQLException {
    try (final var connection = DbConnection.getConnection()) {
      try {
        connection.setAutoCommit(false); // Start transaction

        // Validate that the deck is not yet configured
        validateNonConflictInDeck(connection, deckId, cardIds);

        insertDeckCards(connection, deckId, cardIds); // Add cards to the deck
        new StackDbAccess().updateStacksDeckId(connection, deckId, userId, cardIds); // Update user's stack

        connection.commit(); // Commit the transaction if all steps succeed
        return true;
      } catch (final SQLException e) {
        handleRollback(connection); // Rollback transaction on failure

        // Re-throw specific exceptions or handle them accordingly
        if (e.getMessage().contains("Conflict")) {
          throw new SQLException("Conflict: Deck is already configured.", e);
        } else if (e.getMessage()
            .contains("One of the cards is either not in the user's stack or is part of a trading deal.")) {
          throw e;
        }

        logger.severe("Failed to configure Deck: " + e.getMessage());
        return false; // Indicate failure if not re-throwing
      }
    }
  }

  private void validateNonConflictInDeck(final Connection connection, final UUID deckId, final UUID[] cardIds)
      throws SQLException {
    final String sql = "SELECT COUNT(*) FROM deck_cards WHERE deck_id = ?";
    try (final var stmt = connection.prepareStatement(sql)) {
      stmt.setObject(1, deckId);
      try (final var result = stmt.executeQuery()) {
        if (result.next() && result.getInt(1) + cardIds.length > 4) {
          throw new SQLException("Conflict: The Deck is already configured");
        }
      }
    }
  }

  private void insertDeckCards(final Connection connection, final UUID deckId, final UUID[] cardIds)
      throws SQLException {
    final String insertSQL = "INSERT INTO deck_cards (deck_id, card_id) VALUES (?, ?)";
    try (final var stmt = connection.prepareStatement(insertSQL)) {
      for (final var cardId : cardIds) {
        stmt.setObject(1, deckId);
        stmt.setObject(2, cardId);
        stmt.addBatch();
      }
      stmt.executeBatch();
    } catch (final SQLException e) {
      if ("23505".equals(e.getSQLState())) {
        throw new SQLException("Conflict: Deck is already configured.", e);
      }
      logger.severe("Failed to configure Deck: " + e.getMessage());
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

  public void deleteDeckCards(final Connection connection, final UUID deckId) throws SQLException {
    final String sql = "DELETE FROM deck_cards WHERE deck_id = ?";
    try (final var stmt = connection.prepareStatement(sql)) {
      stmt.setObject(1, deckId);
      final int stackAffectedRows = stmt.executeUpdate();
      if (stackAffectedRows == 0) {
        throw new SQLException("Failed to delete user stack ");
      }
    }
  }
}
