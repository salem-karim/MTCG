package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

import org.mtcg.models.Trade;

public class StackDbAccess {
  private static final Logger logger = Logger.getLogger(StackDbAccess.class.getName());

  public UUID getStackId(final Connection connection, final UUID id) throws SQLException {
    final String sql = "SELECT id FROM stacks WHERE user_id = ?";
    try (final var stmt = connection.prepareStatement(sql)) {
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

  public void moveCardToOtherStack(final Connection connection, final UUID stackId, final UUID otherStackId,
      final UUID cardId)
      throws SQLException {
    final var stmt = connection.prepareStatement(
        "UPDATE stack_cards SET stack_id = ? WHERE stack_id = ? AND card_id = ?");
    stmt.setObject(1, otherStackId);
    stmt.setObject(2, stackId);
    stmt.setObject(3, cardId);
    stmt.executeUpdate();
  }

  public boolean insertCardsIntoStack(final Connection connection, final UUID stackId, final UUID[] cardIds) {
    final String sql = "INSERT INTO stack_cards (stack_id, card_id) VALUES (?, ?)";
    try (final var stmt = connection.prepareStatement(sql)) {
      for (final UUID cardId : cardIds) {
        stmt.setObject(1, stackId);
        stmt.setObject(2, cardId);
        stmt.addBatch();
      }
      stmt.executeBatch();
    } catch (final SQLException e) {
      logger.warning("SQLException: " + e.getMessage());
      return false;
    }
    return true;
  }

  public void updateStacksDeckId(final Connection connection, final UUID deckId, final UUID userId,
      final UUID[] cardIds) throws SQLException {
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

    try {
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
    } catch (final SQLException e) {
      logger.severe("Failed to update user's stack: " + e.getMessage());
      throw e;
    }
  }

  public void updateStacksTradeId(final Connection connection, final Trade trade, final UUID userId)
      throws SQLException {
    final String validateCardSQL = """
            SELECT card_id
            FROM stack_cards sc
            JOIN stacks s ON sc.stack_id = s.id
            WHERE s.user_id = ?
              AND card_id = ?
              AND sc.deck_id IS NULL
              AND sc.trade_id IS NULL
        """;

    final String updateSQL = """
            UPDATE stack_cards
            SET trade_id = ?
            WHERE card_id = ?
              AND stack_id = (
                  SELECT id FROM stacks WHERE user_id = ?
              )
        """;

    try {
      // Validate that the provided card exists in the user's stack,
      // is not part of a deck, and is not part of another trade
      try (final var validateStmt = connection.prepareStatement(validateCardSQL)) {
        validateStmt.setObject(1, userId);
        validateStmt.setObject(2, trade.getCardId());
        final var resultSet = validateStmt.executeQuery();

        if (!resultSet.next()) {
          throw new SQLException(
              "The card is either not in the user's stack, already in a deck, or part of another trade.");
        }
      }

      // Update the stack_cards table to associate the card with the trade
      try (final var updateStmt = connection.prepareStatement(updateSQL)) {
        updateStmt.setObject(1, trade.getId());
        updateStmt.setObject(2, trade.getCardId());
        updateStmt.setObject(3, userId);
        updateStmt.executeUpdate();
      }

      connection.commit();
    } catch (final SQLException e) {
      logger.severe("Failed to update user's stack with trade: " + e.getMessage());
      throw e;
    }
  }

  public boolean validateCard(final Connection connection, final UUID userId, final UUID cardId) {
    try {
      // Validate that the card is in the request user's stack
      final var stmt = connection.prepareStatement("""
          SELECT COUNT(*) FROM stack_cards sc
          JOIN stacks s ON sc.stack_id = s.id
          WHERE s.user_id = ? AND sc.card_id = ?
          """);
      stmt.setObject(1, userId);
      stmt.setObject(2, cardId);

      final var result = stmt.executeQuery();
      result.next();
      return result.getInt(1) != 0;
    } catch (final SQLException e) {
      logger.severe("Failed to get card from user's stack: " + e.getMessage());
      return false;
    }
  }

  public void moveCardsToOtherStack(final Connection connection, final UUID winnerStackId, final UUID looserStackId,
      final UUID[] loosersDecksCardIds) throws SQLException {
    final String sql = """
            UPDATE stack_cards
            SET stack_id = ?, deck_id = NULL
            WHERE stack_id = ? AND card_id = ANY(?)
        """;

    try (final var stmt = connection.prepareStatement(sql)) {
      stmt.setObject(1, winnerStackId);
      stmt.setObject(2, looserStackId);
      stmt.setArray(3, connection.createArrayOf("UUID", loosersDecksCardIds));

      stmt.executeUpdate();
    }
  }
}
