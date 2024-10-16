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
  private final String token;
  private final String username;

  @JsonIgnore // Ignore this field during serialization/deserialization
  private final String password; // This should be hashed
  private int coins;

  @JsonCreator // Constructor for ObjectMapper
  public User(@JsonProperty("Username") String name,
              @JsonProperty("Password") String passwrd) {
    this.token = name + "-mtcgToken"; // Token naming convention
    this.username = name;
    this.password = hashPassword(passwrd); // Hash the password upon user creation
    this.coins = 20; // Default coins
  }

  // Method to hash the password
  private String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  // Method to verify the password during login
  @JsonIgnore // Ignore this method during serialization
  public boolean verifyPassword(String password) {
    return BCrypt.checkpw(password, this.password);
  }

}
