package org.mtcg.db;

import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mtcg.models.User;
import org.mtcg.utils.exceptions.HttpRequestException;

public class UserDbAccess {
  private static final Logger logger = Logger.getLogger(UserDbAccess.class.getName());

  public boolean addUser(final User user) {
    // The usual get Connection make a prepareStatement execute it
    try (Connection connection = DbConnection.getConnection()) {
      connection.setAutoCommit(false);

      final String userSQL = "INSERT INTO users (id, username, password, token) VALUES (?, ?, ?, ?)";
      try (final var preparedStatement = connection.prepareStatement(userSQL)) {
        // Set all the VALUES of the Statement
        preparedStatement.setObject(1, user.getId());
        preparedStatement.setString(2, user.getUsername());
        preparedStatement.setString(3, user.getPassword());
        preparedStatement.setString(4, user.getToken());

        final int userAffectedRows = preparedStatement.executeUpdate();
        if (userAffectedRows == 0) {
          throw new SQLException("Failed to insert user record into the database.");
        }
      }

      final String stackSQL = "INSERT INTO stacks (id, user_id) VALUES (?, ?)";
      try (final var stackStmt = connection.prepareStatement(stackSQL)) {
        // "initialize" the User stack for future queries
        UUID stackId = UUID.randomUUID();
        stackStmt.setObject(1, stackId);
        stackStmt.setObject(2, user.getId());
        int stackAffectedRows = stackStmt.executeUpdate();

        if (stackAffectedRows == 0) {
          throw new SQLException("Failed to initialize user stack in the database.");
        }
      }

      connection.commit(); // Commit transaction if both inserts succeed
      logger.info("User and their stack added successfully: " + user.getUsername());
      return true;

    } catch (SQLException e) {
      logger.severe("Failed to add user or initialize stack: " + e.getMessage());
      try {
        if (!DbConnection.getConnection().getAutoCommit()) {
          DbConnection.getConnection().rollback(); // Rollback transaction on failure
          logger.info("Transaction rolled back due to failure.");
        }
      } catch (SQLException rollbackEx) {
        logger.severe("Rollback failed: " + rollbackEx.getMessage());
      }
      return false;
    }
  }

  public User getUserByUsername(final String username) {
    try (Connection connection = DbConnection.getConnection()) {
      final String sql = "SELECT id, username, password, token FROM users WHERE username = ?";
      final var preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, username);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          final String token = resultSet.getString("token");
          final String hashedPassword = resultSet.getString("password");
          final UUID id = (UUID) resultSet.getObject("id");
          logger.info("User retrieved successfully: " + username);
          return new User(username, token, hashedPassword, id);
        } else {
          logger.warning("User not found: " + username);
        }
      }
    } catch (final SQLException e) {
      logger.log(Level.SEVERE, "SQL error while retrieving user: " + username, e);
    }
    return null;
  }

  public User getUserFromToken(final Map<String, String> headers) throws HttpRequestException {
    // Get token from the header labeled "Authorization"
    final String authorization = headers.get("Authorization");
    if (authorization != null && authorization.startsWith("Bearer ")) {
      final String token = authorization.substring(7);
      try (Connection connection = DbConnection.getConnection()) {
        // Prepare SQL to retrieve the user by token
        final String sql = "SELECT * FROM users WHERE token = ?";
        final var preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setString(1, token);

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
          if (resultSet.next()) {
            // Retrieve values from the ResultSet
            final UUID userId = (UUID) resultSet.getObject("id");
            final String username = resultSet.getString("username");
            final String password = resultSet.getString("password");
            final int coins = resultSet.getInt("coins");

            // Create and return the User object
            User user = new User(username, token, password, userId);
            user.setCoins(coins);
            logger.info("User retrieved successfully!");
            return user;
          }
        }
      } catch (final SQLException e) {
        logger.warning("Failed to retrieve user with token: " + token);
        throw new HttpRequestException("User not found");
      }
    } else {
      throw new HttpRequestException("No Authorization given");
    }
    return null;
  }

}
