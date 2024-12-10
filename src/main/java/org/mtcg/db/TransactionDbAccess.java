package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class TransactionDbAccess {
  private static Logger logger = Logger.getLogger(TransactionDbAccess.class.getName());

  public boolean buyPackage(UUID id) {
    // TODO:
    // add a Transaction Object into the table and insert its id into packages table

    logger.info("Attempting to buy a package");
    Connection connection = null;
    try {
      connection = DbConnection.getConnection();
      connection.setAutoCommit(false);

      // First get all necessary Information from DB
      // Select random not yet sold Package to get its cards and attempt purchase
      final String randomPkgSQL = "SELECT * FROM packages WHERE transaction_id IS NULL ORDER BY RANDOM() LIMIT 1";
      UUID pkgId = null;

      try (var pkgStmt = connection.prepareStatement(randomPkgSQL)) {
        var result = pkgStmt.executeQuery();
        if (result.next()) {
          pkgId = (UUID) result.getObject("id");
        } else {
          logger.warning("No available Packages to be sold");
          return false;
        }
      }

      // Get the cards of the random Package to set their Ids in the stack_cards table
      final int PKG_SIZE = 5;
      var cardIds = new UUID[PKG_SIZE];
      int index = 0;
      final String getPkgCardsSQL = "SELECT id FROM cards " +
          "INNER JOIN package_cards ON cards.id = package_cards.card_id " +
          "WHERE package_cards.package_id = ?";

      try (var cardsStmt = connection.prepareStatement(getPkgCardsSQL)) {
        cardsStmt.setObject(1, pkgId);
        var result = cardsStmt.executeQuery();
        while (result.next()) {
          if (index >= PKG_SIZE)
            throw new IllegalStateException("Package contains more than " + PKG_SIZE + " cards!");
          UUID cardId = (UUID) result.getObject("id");
          cardIds[index] = cardId;
          index++;
        }
        if (index != PKG_SIZE) {
          throw new IllegalStateException("Package contains fewer than " + PKG_SIZE + " cards!");
        }
      }

      final String stackSQL = "SELECT id FROM stack WHERE user_id = ?";
      UUID stackId = null;

      try (var stackStmt = connection.prepareStatement(randomPkgSQL)) {
        var result = stackStmt.executeQuery();
        if (result.next()) {
          stackId = (UUID) result.getObject("id");
        } else {
          logger.warning("Stack of user not found");
          return false;
        }
      }
      // Now Insert into stack_cards
      final String stackCardsSQL = "INSERT INTO stack_cards (stack_id, card_id) VALUES (?, ?)";
      try (final var stackCardsStmt = connection.prepareStatement(stackCardsSQL)) {
        for (UUID uuid : cardIds) {
          stackCardsStmt.setObject(1, uuid);
          stackCardsStmt.setObject(2, stackId);
          stackCardsStmt.addBatch();
        }
        stackCardsStmt.executeBatch();
      }

    } catch (SQLException e) {
      logger.severe("Failed to buy Package: " + e.getMessage());
    }
  }

}
