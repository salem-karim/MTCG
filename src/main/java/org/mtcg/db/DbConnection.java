package org.mtcg.db;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbConnection {
  private static final Logger logger = Logger.getLogger(DbConnection.class.getName());
  private static String DB_URL;
  private static String USER;
  private static String PASSWORD;

  static {
    try {
      String content = new String(Files.readAllBytes(Paths.get("src/main/resources/db.json")));
      JSONObject json = new JSONObject(content);
      DB_URL = json.getString("db_url");
      USER = json.getString("user");
      PASSWORD = json.getString("password");
      logger.info("Database configuration loaded successfully.");
    } catch (IOException | JSONException e) {
      logger.log(Level.SEVERE, "Error reading database configuration", e);
    }
  }

  public static Connection getConnection() throws SQLException {
    logger.info("Attempting to establish a database connection.");
    Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
    logger.info("Database connection established successfully.");
    return connection;
  }

  public static void closeConnection(Connection connection) {
    if (connection != null) {
      try {
        connection.close();
        logger.info("Database connection closed successfully.");
      } catch (SQLException e) {
        logger.log(Level.SEVERE, "Error closing database connection", e);
      }
    }
  }
}
