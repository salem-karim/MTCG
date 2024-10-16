package org.mtcg.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

  // Database credentials
  private static final String DB_URL = "jdbc:postgresql://localhost:5432/mctg_db"; // Update with your DB URL
  private static final String USER = "mctgdb"; // Your PostgreSQL username
  private static final String PASSWORD = "mctgadmin"; // Your PostgreSQL password

  // Method to establish a connection
  public static Connection getConnection() throws SQLException {
    Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
    return connection;
  }

  // Method to close the connection
  public static void closeConnection(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
