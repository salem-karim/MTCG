package org.mtcg.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class UserData {
  @JsonProperty("Name")
  private String username = "";

  @JsonProperty("Bio")
  private String bio = "";

  @JsonProperty("Image")
  private String image = "";

  @JsonCreator
  public UserData(@JsonProperty("Username") final String username,
      @JsonProperty("Bio") final String bio, @JsonProperty("Image") final String image) {
    this.username = username;
    this.bio = bio;
    this.image = image;
  }
}
