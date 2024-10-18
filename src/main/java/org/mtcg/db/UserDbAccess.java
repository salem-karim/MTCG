package org.mtcg.db;

import org.mtcg.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDbAccess {
  private static final Logger logger = Logger.getLogger(UserDbAccess.class.getName());
  Connection connection = null;
  public boolean addUser(User user) {
    logger.info("Attempting to add user: " + user.getUsername());
    try {
      Connection connection = DbConnection.getConnection();
      String sql = "INSERT INTO users (username, password, coins, token) VALUES (?, ?, DEFAULT, ?)";
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, user.getUsername());
      preparedStatement.setString(2, user.getPassword());
      preparedStatement.setString(3, user.getToken());

      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows > 0) {
        logger.info("User added successfully: " + user.getUsername());
        return true;
      } else {
        logger.warning("Failed to add user: " + user.getUsername());
        return false;
      }
    } catch (SQLException e) {
      if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
        logger.warning("Username already exists: " + user.getUsername());
        return false;
      }
      logger.log(Level.SEVERE, "SQL error while adding user: " + user.getUsername(), e);
      return false;
    } finally {
      DbConnection.closeConnection(connection);
    }
  }

  public User getUserByUsername(String username) {
    logger.info("Attempting to retrieve user by username: " + username);
    Connection connection = null;
    try {
      connection = DbConnection.getConnection();
      String sql = "SELECT username, password, token FROM users WHERE username = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, username);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          String token = resultSet.getString("token");
          String hashedPassword = resultSet.getString("password");
          logger.info("User retrieved successfully: " + username);
          return new User(username, token, hashedPassword);
        } else {
          logger.warning("User not found: " + username);
        }
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "SQL error while retrieving user: " + username, e);
    } finally {
      DbConnection.closeConnection(connection);
    }
    return null;
  }
}
