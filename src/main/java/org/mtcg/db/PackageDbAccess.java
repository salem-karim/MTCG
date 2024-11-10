package org.mtcg.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.mtcg.models.Package;
import org.mtcg.utils.exceptions.HttpRequestException;

public class PackageDbAccess {
  private static final Logger logger = Logger.getLogger(PackageDbAccess.class.getName());

  public boolean addPackage(final Package pkg) {
    logger.info("Attempting to add a new package");
    Connection connection = null;

    try {
      connection = DbConnection.getConnection();
      connection.setAutoCommit(false);

      // Insert the package into the package table
      final String insertPackageSQL = "INSERT INTO packages (id, user_id) VALUES (?, ?)";
      try (var packageStmt = connection.prepareStatement(insertPackageSQL)) {
        packageStmt.setObject(1, pkg.getId());
        packageStmt.setObject(2, pkg.getUserId());
        final int rowsAffected = packageStmt.executeUpdate();
        if (rowsAffected != 1) {
          throw new SQLException("Failed to create package record in database.");
        }
      }

      // Insert cards into the cards table using Batches
      final String insertCardSQL = "INSERT INTO cards (id, name, damage, element_type, card_type) VALUES (?, ?, ?, ?, ?)";
      try (var cardStmt = connection.prepareStatement(insertCardSQL)) {
        // Set Values of stamement in a loop
        for (final var card : pkg.getCards()) {
          cardStmt.setObject(1, card.getId());
          cardStmt.setString(2, card.getName());
          cardStmt.setFloat(3, card.getDamage());
          cardStmt.setString(4, card.getElement().name().toLowerCase());
          cardStmt.setString(5, card.getCardType().name().toLowerCase());
          cardStmt.addBatch(); // Add to batch
        }
        cardStmt.executeBatch(); // Execute all insert stamements
      }

      // Insert foreign keys into package_cards table
      final String insertPackageCardsSQL = "INSERT INTO package_cards (package_id, card_id) VALUES (?, ?)";
      try (var packageCardStmt = connection.prepareStatement(insertPackageCardsSQL)) {
        // Again for every card of the Package
        for (final var card : pkg.getCards()) {
          packageCardStmt.setObject(1, pkg.getId());
          packageCardStmt.setObject(2, card.getId());
          packageCardStmt.addBatch(); // Add to batch
        }
        packageCardStmt.executeBatch(); // Execute all inserts statements
      }

      connection.commit(); // Commit transaction if all statements succeed
      logger.info("Package added successfully to the database");
      return true;

    } catch (final SQLException e) {
      logger.severe("Failed to add package: " + e.getMessage());
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

  public UUID getUserId(final Map<String, String> headers) throws HttpRequestException {
    // get token from the header labeled with "Authorization"
    final String authorization = headers.get("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
      final String token = authorization.substring(7);
      try (Connection connection = DbConnection.getConnection()) {
        // Then get its userId using the token
        final String sql = "SELECT id FROM users where token = ?";
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, token);

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          if (resultSet.next()) {
            final UUID userId = (UUID) resultSet.getObject("id");
            logger.info("UserId retrieved successfully!");
            return userId;
          }
        }
      } catch (final SQLException e) {
        logger.warning("User of token not found");
      }
    } else {
      throw new HttpRequestException("No Authorization given");
    }
    return null;
  }
}
