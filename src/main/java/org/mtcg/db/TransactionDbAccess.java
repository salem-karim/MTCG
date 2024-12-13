package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import org.mtcg.models.Transaction;

public class TransactionDbAccess {
  private static Logger logger = Logger.getLogger(TransactionDbAccess.class.getName());

  public boolean buyPackage(UUID id) {

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

      final String stackSQL = "SELECT id FROM stacks WHERE user_id = ?";
      UUID stackId = null;

      try (final var stackStmt = connection.prepareStatement(stackSQL)) {
        stackStmt.setObject(1, id);
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
          stackCardsStmt.setObject(1, stackId);
          stackCardsStmt.setObject(2, uuid);
          stackCardsStmt.addBatch();
        }
        stackCardsStmt.executeBatch();
      }

      final var transaction = new Transaction(UUID.randomUUID(), id, pkgId);

      final String transactionSQL = "INSERT INTO transactions (id, user_id, package_id) VALUES (?, ?, ?)";
      try (final var transactionStmt = connection.prepareStatement(transactionSQL)) {
        transactionStmt.setObject(1, transaction.getId());
        transactionStmt.setObject(2, transaction.getUser_Id());
        transactionStmt.setObject(3, transaction.getPackage_Id());

        final int AffectedRows = transactionStmt.executeUpdate();
        if (AffectedRows == 0) {
          throw new SQLException("Failed to insert transaction record into database");
        }
      }

      final String pkgUpdateSQL = "UPDATE packages SET transaction_id = ? WHERE id = ?";
      try (final var pkgUpdateStmt = connection.prepareStatement(pkgUpdateSQL)) {
        pkgUpdateStmt.setObject(1, transaction.getId());
        pkgUpdateStmt.setObject(2, pkgId);

        final int affectedRows = pkgUpdateStmt.executeUpdate();
        if (affectedRows == 0) {
          throw new SQLException("Failed to update the package with the transaction record");
        }
      }

      connection.commit();
      logger.info("Transaction executed successfully");
      return true;

    } catch (SQLException e) {
      logger.severe("Failed to buy Package: " + e.getMessage());
      try {
        if (connection != null) {
          connection.rollback(); // Rollback transaction in case of error
        }
      } catch (final SQLException rollbackEx) {
        logger.severe("Failed to rollback transaction: " + rollbackEx.getMessage());
      }
      return false;
    } finally {
      try {
        if (connection != null && !connection.isClosed()) {
          connection.setAutoCommit(true); // Restore auto-commit mode
          connection.close(); // Close the connection
        }
      } catch (final SQLException ex) {
        logger.warning("Failed to reset auto-commit or close connection: " + ex.getMessage());
      }

    }
  }

}
