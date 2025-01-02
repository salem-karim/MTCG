package org.mtcg.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mtcg.models.User;
import org.mtcg.models.UserData;
import org.mtcg.models.UserStats;

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

  public User getUserByUsername(final String username) {
    try (final var connection = DbConnection.getConnection()) {
      final String sql = "SELECT * FROM users WHERE username = ?";
      final var preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, username);

      try (final var resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          logger.info("User retrieved successfully: " + username);
          return new User((UUID) resultSet.getObject("id"), username, resultSet.getString("name"),
              resultSet.getString("bio"), resultSet.getString("image"), resultSet.getString("password"),
              resultSet.getInt("coins"), resultSet.getString("token"), resultSet.getInt("elo"),
              resultSet.getInt("wins"), resultSet.getInt("losses"));
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
          final var user = new User((UUID) resultSet.getObject("id"), resultSet.getString("username"),
              resultSet.getString("name"), resultSet.getString("bio"), resultSet.getString("image"),
              resultSet.getString("password"), resultSet.getInt("coins"), token, resultSet.getInt("elo"),
              resultSet.getInt("wins"), resultSet.getInt("losses"));
          logger.info("User retrieved successfully: " + resultSet.getString("username"));
          return user;
        }
      }
    } catch (final SQLException e) {
      logger.warning("Failed to retrieve user with token: " + token);
    } // return null if User was not found or SQL Error happened
    return null;
  }

  public void updateUserCoins(final Connection connection, final User user) throws SQLException {
    final String updateUserCoinsSQL = "UPDATE users SET coins = ? WHERE id = ?";
    try (final var updateCoinsStmt = connection.prepareStatement(updateUserCoinsSQL)) {
      updateCoinsStmt.setInt(1, user.getCoins());
      updateCoinsStmt.setObject(2, user.getId());

      final int affectedRows = updateCoinsStmt.executeUpdate();
      if (affectedRows == 0) {
        throw new SQLException("Failed to update user coins. No user found with the given ID.");
      }
    }
  }

  public boolean updateUserData(final UserData userData, final String username) {
    try (final var connection = DbConnection.getConnection()) {
      final String sql = "UPDATE users SET name = ?, bio = ? , image = ? WHERE username = ?";
      try (final var Stmt = connection.prepareStatement(sql)) {
        Stmt.setString(1, userData.getUsername());
        Stmt.setString(2, userData.getBio());
        Stmt.setString(3, userData.getImage());
        Stmt.setString(4, username);

        final int affectedRows = Stmt.executeUpdate();
        if (affectedRows == 0) {
          throw new SQLException("Failed to update user coins. No user found with the given ID.");
        } else {
          return true;
        }
      }
    } catch (final SQLException e) {
      logger.warning("Failed to update user data of user: " + userData.getUsername());
    }
    return false;
  }

  public void updateUserStats(final Connection connection, final UserStats userStats) throws SQLException {
    final String sql = "UPDATE users SET elo = ?, wins = ? , losses = ? WHERE username = ?";
    try (final var Stmt = connection.prepareStatement(sql)) {
      Stmt.setInt(1, userStats.getElo());
      Stmt.setInt(2, userStats.getWins());
      Stmt.setInt(3, userStats.getLosses());
      Stmt.setString(4, userStats.getUsername());

      final int affectedRows = Stmt.executeUpdate();
      if (affectedRows == 0) {
        throw new SQLException("Failed to update user coins. No user found with the given ID.");
      }
    }
  }

  public List<UserStats> getAllUserStats() {
    final var allUserstats = new ArrayList<UserStats>();
    try (final var connection = DbConnection.getConnection()) {
      final String sql = "SELECT username, elo, wins, losses FROM users ORDER BY elo DESC";
      final var Stmt = connection.prepareStatement(sql);
      try (final var result = Stmt.executeQuery()) {
        while (result.next()) {
          allUserstats.add(new UserStats(
              result.getString("username"),
              result.getInt("elo"),
              result.getInt("wins"),
              result.getInt("losses")));
        }
      }
      return allUserstats;
    } catch (final SQLException e) {
      logger.warning("Failed to get all Users Stats: " + e.getMessage());
      return null;
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
}
