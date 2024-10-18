package org.mtcg.db;

import org.mtcg.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDbAccess {

  // Method to add a user
  public boolean addUser(User user) {
    try (Connection connection = DbConnection.getConnection()) {
      String sql = "INSERT INTO users (username, password, coins, token) VALUES (?, ?, DEFAULT, ?)";
      PreparedStatement preparedStatement = connection.prepareStatement(sql);

      // Set parameters
      preparedStatement.setString(1, user.getUsername());
      preparedStatement.setString(2, user.getPassword()); // Use the hashed password from User
      preparedStatement.setString(3, user.getToken());

      int affectedRows = preparedStatement.executeUpdate();

      // If one row is affected, the user was added successfully
      return affectedRows > 0;
    } catch (SQLException e) {
      // Check for unique constraint violation based on the error message
      if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
        return false; // Username already exists
      }
      e.printStackTrace();
      return false; // Handle other SQL errors
    }
  }


  // Method to get a user by username
  public User getUserByUsername(String username) {
    try (Connection connection = DbConnection.getConnection()) {
      String sql = "SELECT username, password, token FROM users WHERE username = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, username);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          String token = resultSet.getString("token");
          String hashedPassword = resultSet.getString("password");

          // Create a User instance without exposing the password
          return new User(username, token, hashedPassword);
        }
      }
    } catch (SQLException e) {
      e.printStackTrace(); // Log the exception
    }
    return null; // Return null if user is not found
  }


}