package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import org.mtcg.models.User;

public class TransactionDbAccess {
  private static final Logger logger = Logger.getLogger(TransactionDbAccess.class.getName());
  private final PackageDbAccess pkgDbAccess = new PackageDbAccess();
  private final StackDbAccess stackDbAccess = new StackDbAccess();
  private final UserDbAccess userDbAccess = new UserDbAccess();

  // Retrieve a random package ID
  public UUID getRandomPackage() throws SQLException {
    return pkgDbAccess.getRandomPackage(DbConnection.getConnection()); // Individual SELECT query without a persistent
                                                                       // connection
  }

  // Retrieve card IDs for the specified package
  public UUID[] getPackageCards(final UUID packageId) throws SQLException {
    return pkgDbAccess.getPackageCards(DbConnection.getConnection(), packageId); // Individual SELECT query
  }

  // Retrieve the user's stack ID
  public UUID getStackId(final UUID userId) throws SQLException {
    return stackDbAccess.getStackId(DbConnection.getConnection(), userId); // Individual SELECT query
  }

  // Perform the entire transaction within a single connection
  public void performTransaction(final User user, final UUID packageId, final UUID[] cardIds, final UUID stackId)
      throws SQLException {
    logger.info("Performing transaction...");
    try (final Connection connection = DbConnection.getConnection()) {
      connection.setAutoCommit(false); // Start transaction

      try {
        // Insert cards into user's stack
        if (!stackDbAccess.insertCardsIntoStack(connection, stackId, cardIds)) {
          throw new SQLException("Failed to insert cards into stack");
        }

        // Create transaction record
        final UUID transactionId = processTransaction(connection, user.getId(), packageId);
        if (transactionId == null) {
          throw new SQLException("Failed to create transaction record");
        }

        // Finalize package update
        if (!finalizePackage(connection, packageId, transactionId)) {
          throw new SQLException("Failed to update package with transaction ID");
        }

        // Update user coins
        try {
          userDbAccess.updateUserCoins(connection, user);
        } catch (final SQLException e) {
          throw new SQLException("Failed to update user coins");
        }

        connection.commit(); // Commit transaction
        logger.info("Transaction executed successfully");

      } catch (SQLException e) {
        connection.rollback(); // Rollback in case of failure
        logger.severe("Transaction failed: " + e.getMessage());
        throw e; // Rethrow exception to propagate error
      }
    }
  }

  private UUID processTransaction(final Connection connection, final UUID userId, final UUID packageId)
      throws SQLException {
    final UUID transactionId = UUID.randomUUID();
    final String transactionSQL = "INSERT INTO transactions (id, user_id, package_id) VALUES (?, ?, ?)";
    try (final var stmt = connection.prepareStatement(transactionSQL)) {
      stmt.setObject(1, transactionId);
      stmt.setObject(2, userId);
      stmt.setObject(3, packageId);
      if (stmt.executeUpdate() == 0) {
        logger.warning("Failed to create transaction");
        return null;
      }
    }
    return transactionId;
  }

  private boolean finalizePackage(final Connection connection, final UUID packageId, final UUID transactionId)
      throws SQLException {
    final String pkgUpdateSQL = "UPDATE packages SET transaction_id = ? WHERE id = ?";
    try (final var stmt = connection.prepareStatement(pkgUpdateSQL)) {
      stmt.setObject(1, transactionId);
      stmt.setObject(2, packageId);
      return stmt.executeUpdate() > 0;
    }
  }
}
