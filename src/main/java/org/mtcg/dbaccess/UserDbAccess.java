package org.mtcg.dbaccess;

import org.mtcg.db.DbConnection;
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
    User user = null;
    try (Connection connection = DbConnection.getConnection()) {
      String sql = "SELECT username, password, coins FROM users WHERE username = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(sql);

      // Set parameters
      preparedStatement.setString(1, username);

      try (ResultSet resultSet = preparedStatement.executeQuery()) {
        if (resultSet.next()) {
          // Retrieve user data
          String password = resultSet.getString("password");
          int coins = resultSet.getInt("coins");

          // Create and return the User object
          user = new User(username, password);
//          user.setCoins(coins); // Assuming you have a setter for coins
        }
      }
    } catch (SQLException e) {
      e.printStackTrace(); // Log the exception
    }

    return user; // Return the user object or null if not found
  }
}
