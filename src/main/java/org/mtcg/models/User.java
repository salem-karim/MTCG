package org.mtcg.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mindrot.jbcrypt.BCrypt;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
  private String token;
  private String username;

  @JsonIgnore // Ignore this field during serialization/deserialization
  private String password; // This should be hashed
  private int coins;

  // Constructor for ObjectMapper
  @JsonCreator
  public User(@JsonProperty("Username") final String name,
      @JsonProperty("Password") final String Password) {
    this.token = name + "-mtcgToken"; // Token naming convention
    this.username = name;
    this.password = hashPassword(Password); // Hash the password upon user creation
    this.coins = 20; // Default coins
  }

  // Additional constructor for retrieving from the database
  public User(final String username, final String token, final String Password) {
    this.token = token;
    this.username = username;
    this.password = Password;
    this.coins = 20; // Default coins
  }

  // Method to hash the password
  private String hashPassword(final String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  // Method to verify the password during login
  @JsonIgnore // Ignore this method during serialization
  public boolean verifyPassword(final String password) {
    return BCrypt.checkpw(password, this.password);
  }
}
