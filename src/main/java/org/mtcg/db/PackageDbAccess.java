package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import org.mtcg.models.Package;

public class PackageDbAccess {
  private static final Logger logger = Logger.getLogger(PackageDbAccess.class.getName());
  private final CardDbAccess cardDbAccess = new CardDbAccess();

  public boolean addPackage(final Package pkg) throws SQLException {
    logger.info("Attempting to add a new package");

    try (final var connection = DbConnection.getConnection()) {
      try {
        connection.setAutoCommit(false);

        // Perform all the necessary database operations
        insertPackage(connection, pkg);
        cardDbAccess.insertCards(connection, pkg);
        insertPackageCards(connection, pkg);

        // Commit transaction if all operations succeed
        connection.commit();
        logger.info("Package added successfully to the database");
        return true;

      } catch (final SQLException e) {
        logger.severe("Failed to add package: " + e.getMessage());
        if ("23505".equals(e.getSQLState())) {
          throw new SQLException("Conflict: Card with this ID already exists in this Package.", e);
        }
        handleRollback(connection);
        return false; // Indicate failure
      }
    } catch (final SQLException e) {
      if ("23505".equals(e.getSQLState())) {
        throw new SQLException("Conflict: Card with this ID already exists in this Package.", e);
      }
      logger.severe("Failed to add Package: " + e.getMessage());
      return false;
    }

  }

  public UUID getRandomPackage(final Connection connection) throws SQLException {
    final String randomPkgSQL = "SELECT * FROM packages WHERE transaction_id IS NULL LIMIT 1";
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

  public UUID[] getPackageCards(final Connection connection, final UUID pkgId) throws SQLException {
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

  private void handleRollback(final Connection con) {
    try {
      if (!con.getAutoCommit()) {
        con.rollback();
        logger.info("Transaction rolled back due to failure.");
      }
    } catch (final SQLException rollbackEx) {
      logger.severe("Rollback failed: " + rollbackEx.getMessage());
    }
  }

  // Insert the package into the `packages` table
  private void insertPackage(final Connection connection, final Package pkg) throws SQLException {
    final String insertPackageSQL = "INSERT INTO packages (id, user_id) VALUES (?, ?)";
    try (final var packageStmt = connection.prepareStatement(insertPackageSQL)) {
      packageStmt.setObject(1, pkg.getId());
      packageStmt.setObject(2, pkg.getUserId());
      final int rowsAffected = packageStmt.executeUpdate();
      if (rowsAffected == 0) {
        throw new SQLException("Failed to create package record in database.");
      }
    }
  }

  // Insert relationships into the `package_cards` table
  private void insertPackageCards(final Connection connection, final Package pkg) throws SQLException {
    final String insertPackageCardsSQL = "INSERT INTO package_cards (package_id, card_id) VALUES (?, ?)";
    try (final var packageCardStmt = connection.prepareStatement(insertPackageCardsSQL)) {
      for (final var card : pkg.getCards()) {
        packageCardStmt.setObject(1, pkg.getId());
        packageCardStmt.setObject(2, card.getId());
        packageCardStmt.addBatch();
      }
      packageCardStmt.executeBatch(); // Execute all insert statements
    }
  }

}
