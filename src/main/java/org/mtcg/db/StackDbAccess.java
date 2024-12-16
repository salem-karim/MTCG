package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class StackDbAccess {
  private static final Logger logger = Logger.getLogger(StackDbAccess.class.getName());

  public UUID getStackId(Connection connection, UUID id) throws SQLException {
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

  public UUID getUserStack(final Connection connection, final UUID userId) throws SQLException {
    final String stackSQL = "SELECT id FROM stacks WHERE user_id = ?";
    try (final var stmt = connection.prepareStatement(stackSQL)) {
      stmt.setObject(1, userId);
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

}
