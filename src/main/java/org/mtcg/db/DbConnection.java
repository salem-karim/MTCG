package org.mtcg.db;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
    try (InputStream inputStream = DbConnection.class.getClassLoader().getResourceAsStream("db/mtcg.json")) {
      if (inputStream == null) {
        throw new IllegalArgumentException("Database configuration file not found.");
      }
      String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
      JSONObject json = new JSONObject(content);
      DB_URL = json.getString("db_url");
      USER = json.getString("user");
      PASSWORD = json.getString("password");
      logger.info("Database configuration loaded successfully.");
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Error reading database configuration", e);
    }
  }

  public static Connection getConnection() throws SQLException {
    logger.info("Attempting to establish a database connection.");
    Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
    logger.info("Database connection established successfully.");
    return connection;
  }
}
