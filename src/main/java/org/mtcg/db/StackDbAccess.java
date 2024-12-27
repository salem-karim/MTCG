package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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

  public Set<UUID> getUserStack(final UUID userId) throws SQLException {
    try (final var connection = DbConnection.getConnection()) {
      final var stackId = getStackId(connection, userId);
      final Set<UUID> cardIds = new HashSet<>();
      final String stackSQL = "SELECT * FROM stack_cards WHERE stack_id = ?";
      try (final var stmt = connection.prepareStatement(stackSQL)) {
        stmt.setObject(1, stackId);
        try (final var result = stmt.executeQuery()) {
          while (result.next()) {
            cardIds.add((UUID) result.getObject("card_id"));
          }
        }
      }
      return cardIds;
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
