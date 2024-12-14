package org.mtcg.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.mtcg.models.Package;

public class PackageDbAccess {
  private static final Logger logger = Logger.getLogger(PackageDbAccess.class.getName());

  public boolean addPackage(final Package pkg) {
    logger.info("Attempting to add a new package");

    try (final var connection = DbConnection.getConnection()) {
      connection.setAutoCommit(false);

      // Perform all the necessary database operations
      insertPackage(connection, pkg);
      insertCards(connection, pkg);
      insertPackageCards(connection, pkg);

      // Commit transaction if all operations succeed
      connection.commit();
      logger.info("Package added successfully to the database");
      return true;

    } catch (final SQLException e) {
      logger.severe("Failed to add package: " + e.getMessage());
      handleRollback();
      return false; // Indicate failure
    }
  }

  private void handleRollback() {
    try {
      final Connection connection = DbConnection.getConnection();
      if (!connection.getAutoCommit()) {
        connection.rollback();
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
      if (rowsAffected != 1) {
        throw new SQLException("Failed to create package record in database.");
      }
    }
  }

  // Insert cards into the `cards` table using batch execution
  private void insertCards(final Connection connection, final Package pkg) throws SQLException {
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
