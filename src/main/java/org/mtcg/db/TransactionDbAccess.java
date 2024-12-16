package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class TransactionDbAccess {
  private static Logger logger = Logger.getLogger(TransactionDbAccess.class.getName());

  public boolean buyPackage(final UUID userId) {
    logger.info("Attempting to buy a package");
    try (Connection connection = DbConnection.getConnection()) {
      connection.setAutoCommit(false); // Start transaction

      // Step 1: Get package and card info
      final UUID pkgId = getRandomPackage(connection);
      if (pkgId == null) {
        return false; // No package to buy
      }

      final UUID[] cardIds = getPackageCards(connection, pkgId);
      if (cardIds == null) {
        return false; // Failed to retrieve package cards
      }

      // Step 2: Get user's stack and insert cards
      final UUID stackId = getUserStack(connection, userId);
      if (stackId == null) {
        return false; // No stack for user
      }

      if (!insertCardsIntoStack(connection, stackId, cardIds)) {
        return false; // Card insertion failed
      }

      // Step 3: Process the transaction
      final UUID transactionId = processTransaction(connection, userId, pkgId);
      if (transactionId == null) {
        return false; // Transaction creation failed
      }

      // Step 4: Finalize package update
      if (!finalizePackage(connection, pkgId, transactionId)) {
        return false; // Package update failed
      }

      connection.commit(); // Commit transaction
      logger.info("Transaction executed successfully");
      return true;

    } catch (final SQLException e) {
      logger.severe("Failed to buy Package: " + e.getMessage());
      // Explicit rollback in case of error
      try (Connection connection = DbConnection.getConnection()) {
        connection.rollback(); // Rollback transaction if something went wrong
        logger.info("Rollback successful");
      } catch (final SQLException rollbackEx) {
        logger.severe("Failed to rollback transaction: " + rollbackEx.getMessage());
      }
      return false;
    }
  }

  // Step 1: Get random package from the database
  private UUID getRandomPackage(final Connection connection) throws SQLException {
    final String randomPkgSQL = "SELECT * FROM packages WHERE transaction_id IS NULL ORDER BY RANDOM() LIMIT 1";
    try (final var stmt = connection.prepareStatement(randomPkgSQL);
        var result = stmt.executeQuery()) {
      if (result.next()) {
        return (UUID) result.getObject("id");
      } else {
        logger.warning("No available Packages to be sold");
        return null;
      }
    }
  }

  // Step 1: Get the card IDs associated with the package
  private UUID[] getPackageCards(final Connection connection, final UUID pkgId) throws SQLException {
    int index = 0;
    final int PKG_SIZE = 5;
    final var cardIds = new UUID[PKG_SIZE];
    final String getPkgCardsSQL = "SELECT id FROM cards " +
        "INNER JOIN package_cards ON cards.id = package_cards.card_id " +
        "WHERE package_cards.package_id = ?";

    try (final var stmt = connection.prepareStatement(getPkgCardsSQL)) {
      stmt.setObject(1, pkgId);
      try (final var result = stmt.executeQuery()) {
        while (result.next()) {
          if (index >= PKG_SIZE)
            throw new IllegalStateException("Package contains more than " + PKG_SIZE + " cards!");
          cardIds[index] = (UUID) result.getObject("id");
          index++;
        }
        if (index != PKG_SIZE) {
          throw new IllegalStateException("Package contains fewer than " + PKG_SIZE + " cards!");
        }
      }
    }
    return cardIds;
  }

  // Step 2: Get the user's stack from the database
  private UUID getUserStack(final Connection connection, final UUID userId) throws SQLException {
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

  // Step 2: Insert cards into the user's stack
  private boolean insertCardsIntoStack(final Connection connection, final UUID stackId, final UUID[] cardIds)
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

  // Step 3: Create a transaction for the purchase
  private UUID processTransaction(final Connection connection, final UUID userId, final UUID pkgId)
      throws SQLException {
    final UUID transactionId = UUID.randomUUID(); // Generate transaction ID
    final String transactionSQL = "INSERT INTO transactions (id, user_id, package_id) VALUES (?, ?, ?)";
    try (final var stmt = connection.prepareStatement(transactionSQL)) {
      stmt.setObject(1, transactionId);
      stmt.setObject(2, userId);
      stmt.setObject(3, pkgId);
      final int affectedRows = stmt.executeUpdate();
      if (affectedRows == 0) {
        logger.warning("Transaction creation failed");
        return null;
      }
    }
    return transactionId;
  }

  // Step 4: Finalize the package by updating its transaction
  private boolean finalizePackage(final Connection connection, final UUID pkgId, final UUID transactionId)
      throws SQLException {
    final String pkgUpdateSQL = "UPDATE packages SET transaction_id = ? WHERE id = ?";
    try (final var stmt = connection.prepareStatement(pkgUpdateSQL)) {
      stmt.setObject(1, transactionId);
      stmt.setObject(2, pkgId);
      final int affectedRows = stmt.executeUpdate();
      if (affectedRows == 0) {
        logger.warning("Failed to update package with transaction ID");
        return false;
      }
    }
    return true;
  }

}
