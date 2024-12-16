package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import org.mtcg.models.User;

public class TransactionDbAccess {
  private static Logger logger = Logger.getLogger(TransactionDbAccess.class.getName());
  private final PackageDbAccess pkgdDbAccess = new PackageDbAccess();
  private final StackDbAccess stackDbAccess = new StackDbAccess();
  private final UserDbAccess userDbAccess = new UserDbAccess();

  public boolean buyPackage(final User user) {
    logger.info("Attempting to buy a package");
    try (final var connection = DbConnection.getConnection()) {
      connection.setAutoCommit(false); // Start transaction

      try {
        // Step 2: Get package and card info
        final UUID pkgId = pkgdDbAccess.getRandomPackage(connection);
        if (pkgId == null) {
          return false; // No package to buy
        }

        final UUID[] cardIds = pkgdDbAccess.getPackageCards(connection, pkgId);
        if (cardIds == null) {
          return false; // Failed to retrieve package cards
        }

        // Step 3: Get user's stack and insert cards
        final UUID stackId = stackDbAccess.getUserStack(connection, user.getId());
        if (stackId == null) {
          return false; // No stack for user
        }

        if (!stackDbAccess.insertCardsIntoStack(connection, stackId, cardIds)) {
          return false; // Card insertion failed
        }

        // Step 4: Process the transaction
        final UUID transactionId = processTransaction(connection, user.getId(), pkgId);
        if (transactionId == null) {
          return false; // Transaction creation failed
        }

        // Step 5: Finalize package update
        if (!finalizePackage(connection, pkgId, transactionId)) {
          return false; // Package update failed
        }

        if (!userDbAccess.updateUserCoins(connection, user)) {
          return false; // User Coins update failed
        }

        connection.commit(); // Commit transaction
        logger.info("Transaction executed successfully");
        return true;

      } catch (final SQLException e) {
        logger.severe("Error during transaction: " + e.getMessage());
        connection.rollback(); // Rollback transaction here, while the connection is still open
        logger.info("Rollback successful");
        return false;
      }
    } catch (final SQLException e) {
      logger.severe("Failed to buy Package: " + e.getMessage());
      return false;
    }
  }

  // Step 4: Create a transaction for the purchase
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

  // Step 5: Finalize the package by updating its transaction
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
