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
      final String sql = "INSERT INTO users (id, username, password, token) VALUES (?, ?, ?, ?)";
      final PreparedStatement preparedStatement = connection.prepareStatement(sql);
      // Set all the VALUES of the Statement
      preparedStatement.setObject(1, user.getId());
      preparedStatement.setString(2, user.getUsername());
      preparedStatement.setString(3, user.getPassword());
      preparedStatement.setString(4, user.getToken());

      final int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows > 0) {
        logger.info("User added successfully: " + user.getUsername());
        return true;
      } else {
        logger.warning("Failed to add user: " + user.getUsername());
        return false;
      }
    } catch (final SQLException e) {
      if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
        logger.warning("Username already exists: " + user.getUsername());
        return false;
      }
      logger.log(Level.SEVERE, "SQL error while adding user: " + user.getUsername(), e);
      return false;
    }
  }

  public User getUserByUsername(final String username) {
    try (Connection connection = DbConnection.getConnection()) {
      final String sql = "SELECT id, username, password, token FROM users WHERE username = ?";
      final PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
