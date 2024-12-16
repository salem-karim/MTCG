package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import org.mtcg.models.Card;

public class CardDbAccess {
  private static final Logger logger = Logger.getLogger(CardDbAccess.class.getName());

  public ArrayList<Card> getCards(UUID id) {
    var cardList = new ArrayList<Card>();
    try (final var connection = DbConnection.getConnection()) {
      UUID stackId = getStackId(connection, id);
      final String usersCardsSQL = "SELECT * FROM cards " +
          "INNER JOIN stack_cards ON cards.id = stack_cards.card_id " +
          "WHERE stack_cards.stack_id = ?";
      try (final var UserCardsStmt = connection.prepareStatement(usersCardsSQL)) {
        UserCardsStmt.setObject(1, stackId);
        try (final var result = UserCardsStmt.executeQuery()) {
          while (result.next()) {
            var card = new Card(
                (UUID) result.getObject("id"),
                result.getString("name"),
                result.getDouble("damage"));
            cardList.add(card);
          }
        }
      }
      logger.info("Transaction executed successfully");
      return cardList;
    } catch (SQLException e) {
      logger.severe("Failed to list Users Cards: " + e.getMessage());
      return null;
    }
  }

  private UUID getStackId(Connection connection, UUID id) throws SQLException {
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
}
