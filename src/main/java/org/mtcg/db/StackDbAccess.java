package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

public class StackDbAccess {
  private static final Logger logger = Logger.getLogger(StackDbAccess.class.getName());

  public UUID getStackId(final Connection connection, final UUID id) throws SQLException {
    final String getStackIdSQL = "SELECT id FROM stacks WHERE user_id = ?";
    try (final var stmt = connection.prepareStatement(getStackIdSQL)) {
      stmt.setObject(1, id);
      try (final var result = stmt.executeQuery()) {
        if (result.next()) {
          return (UUID) result.getObject("id");
        } else {
          logger.warning("User does not have a stack");
          return null;
        }
      }
    }
  }

  public boolean insertCardsIntoStack(final Connection connection, final UUID stackId, final UUID[] cardIds)
      throws SQLException {
    final String stackCardsSQL = "INSERT INTO stack_cards (stack_id, card_id) VALUES (?, ?)";
    try (final var stmt = connection.prepareStatement(stackCardsSQL)) {
      for (final UUID cardId : cardIds) {
        stmt.setObject(1, stackId);
        stmt.setObject(2, cardId);
        stmt.addBatch();
      }
      stmt.executeBatch();
    }
    return true;
  }

  public void updateStacksDeckId(final UUID deckId, final UUID userId, final UUID[] cardIds) throws SQLException {
    final String validateCardsSQL = """
            SELECT card_id
            FROM stack_cards sc
            JOIN stacks s ON sc.stack_id = s.id
            WHERE s.user_id = ?
              AND card_id = ANY(?)
              AND sc.trade_id IS NULL
        """;

    final String updateSQL = """
            UPDATE stack_cards
            SET deck_id = ?
            WHERE card_id = ANY(?)
              AND stack_id = (
                  SELECT id FROM stacks WHERE user_id = ?
              )
        """;

    try (final var connection = DbConnection.getConnection()) {
      connection.setAutoCommit(false);

      // Validate that all provided card IDs exist in the user's stack and are not
      // part of any trading deals
      try (final var validateStmt = connection.prepareStatement(validateCardsSQL)) {
        validateStmt.setObject(1, userId);
        validateStmt.setObject(2, connection.createArrayOf("UUID", cardIds));
        final var resultSet = validateStmt.executeQuery();

        final var validCards = new HashSet<UUID>();
        while (resultSet.next()) {
          validCards.add(UUID.fromString(resultSet.getString("card_id")));
        }

        if (validCards.size() != cardIds.length) {
          throw new SQLException("One of the cards is either not in the user's stack or is part of a trading deal.");
        }
      }

      // Update the stack_cards table to associate the valid cards with the given deck
      try (final var updateStmt = connection.prepareStatement(updateSQL)) {
        updateStmt.setObject(1, deckId);
        updateStmt.setObject(2, connection.createArrayOf("UUID", cardIds));
        updateStmt.setObject(3, userId);
        updateStmt.executeUpdate();
      }

      connection.commit();
    } catch (SQLException e) {
      logger.severe("Failed to update user's stack: " + e.getMessage());
      throw e;
    }
  }

}
