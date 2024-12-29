package org.mtcg.db;

import java.sql.SQLException;
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
            final var deckId = (UUID) result.getObject("id");
            return deckId;
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
            final var card = new Card(
                (UUID) result.getObject("id"),
                result.getString("name"),
                result.getDouble("damage"));
            cards[index] = card;
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
      return new Deck(cards, deckId);
    } catch (final IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  public boolean configureDeck(final UUID deckId, final UUID[] cardIds) throws SQLException {
    try (final var connection = DbConnection.getConnection()) {
      final String insertSQL = "INSERT INTO deck_cards (deck_id, card_id) VALUES (?, ?)";
      try (final var stmt = connection.prepareStatement(insertSQL)) {
        for (final var cardId : cardIds) {
          stmt.setObject(1, deckId);
          stmt.setObject(2, cardId);
          stmt.addBatch();
        }
        stmt.executeBatch();
        return true;
      }
    } catch (final SQLException e) {
      if ("23505".equals(e.getSQLState())) {
        throw new SQLException("Conflict: Deck is already configured.", e);
      } else if (e.getMessage().contains("A deck can only contain 4 cards")) {
        throw new SQLException(e);
      }
      logger.severe("Failed to configure Deck: " + e.getMessage());
      return false;
    }
  }
}
