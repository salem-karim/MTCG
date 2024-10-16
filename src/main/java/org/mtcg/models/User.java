package org.mtcg.models;

import lombok.Getter;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;

@Getter @Setter
public class User {
  private final String token;
  private final String username;
  private final String password; // This should be hashed
  private int coins;

  public User(String name, String passwrd) {
    this.token = name + "-mtcgTocken";
    this.username = name;
    this.password = hashPassword(passwrd); // Hash the password upon user creation
    this.coins = 20; // Default coins
  }

  // Method to hash the password
  private String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  // Method to verify the password during login
  public boolean verifyPassword(String password) {
    return BCrypt.checkpw(password, this.password);
  }
}
