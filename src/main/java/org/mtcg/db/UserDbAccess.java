package org.mtcg.db;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mtcg.models.User;

public class UserDbAccess {
  private static final Logger logger = Logger.getLogger(UserDbAccess.class.getName());

  public boolean addUser(final User user) throws SQLException {
    final var connection = DbConnection.getConnection();
    try {
      connection.setAutoCommit(false);
      // Add the user to the database
      addUserToDatabase(connection, user);
      // Initialize the user's stack
      initializeUserStack(connection, user);
      // Initialize the user's deck
      initializeUserDeck(connection, user);
      // Commit the transaction if all operations succeed
      connection.commit();
      logger.info("User and their stack added successfully: " + user.getUsername());
      return true;
    } catch (final SQLException e) {
      // Check for unique constraint violation (PostgreSQL SQL state 23505)
      if ("23505".equals(e.getSQLState())) {
        throw new SQLException("Conflict: User with this ID or username already exists.", e);
      }
      // Handle other exceptions
      logger.severe("Failed to add user or initialize stack: " + e.getMessage());
      handleRollback(connection);
      return false;
    } finally {
      connection.close();
    }
  }

  private void initializeUserDeck(final Connection connection, final User user) throws SQLException {
    final String deckSQL = "INSERT INTO decks (id, user_id) VALUES (?, ?)";
    try (final var deckStmt = connection.prepareStatement(deckSQL)) {
      final UUID deckId = UUID.randomUUID();
      deckStmt.setObject(1, deckId);
      deckStmt.setObject(2, user.getId());

      final int stackAffectedRows = deckStmt.executeUpdate();
      if (stackAffectedRows == 0) {
        throw new SQLException("Failed to initialize user stack in the database.");
      }
    }
  }

  private void addUserToDatabase(final Connection connection, final User user) throws SQLException {
    final String userSQL = "INSERT INTO users (id, username, password, token) VALUES (?, ?, ?, ?)";
    try (final var preparedStatement = connection.prepareStatement(userSQL)) {

      preparedStatement.setObject(1, user.getId());
      preparedStatement.setString(2, user.getUsername());
      preparedStatement.setString(3, user.getPassword());
      preparedStatement.setString(4, user.getToken());

      final int userAffectedRows = preparedStatement.executeUpdate();
      if (userAffectedRows == 0) {
        throw new SQLException("Failed to insert user record into the database.");
      }
    }
  }

  private void initializeUserStack(final Connection connection, final User user) throws SQLException {
    final String stackSQL = "INSERT INTO stacks (id, user_id) VALUES (?, ?)";
    try (final var stackStmt = connection.prepareStatement(stackSQL)) {
      final UUID stackId = UUID.randomUUID();
      stackStmt.setObject(1, stackId);
      stackStmt.setObject(2, user.getId());

      final int stackAffectedRows = stackStmt.executeUpdate();
      if (stackAffectedRows == 0) {
        throw new SQLException("Failed to initialize user stack in the database.");
      }
    }
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

  public User getUserByUsername(final String username) {
    try (final var connection = DbConnection.getConnection()) {
      final String sql = "SELECT id, username, password, token FROM users WHERE username = ?";
      final var preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, username);

      try (final var resultSet = preparedStatement.executeQuery()) {
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

  public User getUserFromToken(final String token) {
    if (token == null || token.isEmpty()) {
      return null;
    }
    try (final var connection = DbConnection.getConnection()) {
      // Prepare SQL to retrieve the user by token
      final String sql = "SELECT * FROM users WHERE token = ?";
      final var preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, token);

      try (final var resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          // Retrieve values from the ResultSet
          final UUID userId = (UUID) resultSet.getObject("id");
          final String username = resultSet.getString("username");
          final String password = resultSet.getString("password");
          final int coins = resultSet.getInt("coins");

          // Create and return the User object
          final User user = new User(username, token, password, userId);
          user.setCoins(coins);
          logger.info("User retrieved successfully!");
          return user;
        }
      }
    } catch (final SQLException e) {
      logger.warning("Failed to retrieve user with token: " + token);
    } // return null if User was not found or SQL Error happened
    return null;
  }

  public boolean updateUserCoins(final Connection connection, final User user) throws SQLException {
    final String updateUserCoinsSQL = "UPDATE users SET coins = ? WHERE id = ?";
    try (final var updateCoinsStmt = connection.prepareStatement(updateUserCoinsSQL)) {
      updateCoinsStmt.setInt(1, user.getCoins());
      updateCoinsStmt.setObject(2, user.getId());

      final int affectedRows = updateCoinsStmt.executeUpdate();
      if (affectedRows == 0) {
        throw new SQLException("Failed to update user coins. No user found with the given ID.");
      }
      return true;
    }
  }

}
