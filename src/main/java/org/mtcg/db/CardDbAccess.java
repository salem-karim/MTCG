package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mtcg.models.Card;
import org.mtcg.models.Package;

public class CardDbAccess {
  private static final Logger logger = Logger.getLogger(CardDbAccess.class.getName());
  private final StackDbAccess stackDbAccess = new StackDbAccess();

  public ArrayList<Card> getCards(final UUID id) {
    final var cardList = new ArrayList<Card>();
    try (final var connection = DbConnection.getConnection()) {
      final UUID stackId = stackDbAccess.getStackId(connection, id);
      final String usersCardsSQL = "SELECT * FROM cards " +
          "INNER JOIN stack_cards ON cards.id = stack_cards.card_id " +
          "WHERE stack_cards.stack_id = ?";
      try (final var UserCardsStmt = connection.prepareStatement(usersCardsSQL)) {
        UserCardsStmt.setObject(1, stackId);
        try (final var result = UserCardsStmt.executeQuery()) {
          while (result.next()) {
            cardList.add(new Card(
                (UUID) result.getObject("id"),
                result.getString("name"),
                result.getDouble("damage")));
          }
        }
      }
      logger.info("Transaction executed successfully");
      return cardList;
    } catch (final SQLException e) {
      logger.severe("Failed to list Users Cards: " + e.getMessage());
      return null;
    }
  }

  public void insertCards(final Connection connection, final Package pkg) throws SQLException {
    final String insertCardSQL = "INSERT INTO cards (id, name, damage, element_type, card_type) VALUES (?, ?, ?, ?, ?)";
    try (final var cardStmt = connection.prepareStatement(insertCardSQL)) {
      for (final var card : pkg.getCards()) {
        cardStmt.setObject(1, card.getId());
        cardStmt.setString(2, card.getName());
        cardStmt.setDouble(3, card.getDamage()); // No `final var` here for primitives
        cardStmt.setString(4, card.getElement().name().toLowerCase());
        cardStmt.setString(5, card.getCardType().name().toLowerCase());
        cardStmt.addBatch();
      }
      cardStmt.executeBatch(); // Execute all insert statements
    }
  }

  public Card getCardById(UUID cardId) {
    try (final var connection = DbConnection.getConnection()) {
      final String IdSQL = "SELECT * FROM cards WHERE id = ?";

      try (final var IdStmt = connection.prepareStatement(IdSQL)) {
        IdStmt.setObject(1, cardId);
        try (final var result = IdStmt.executeQuery()) {
          if (result.next()) {
            return new Card(
                (UUID) result.getObject("id"),
                result.getString("name"),
                result.getDouble("damage"));
          } else {
            logger.warning("Card with ID: " + cardId + "not found\n");
          }
        }
      }
    } catch (final SQLException e) {
      logger.log(Level.SEVERE, "SQL error while retrieving card with ID: " + cardId, e);
    }
    return null;
  }
}
