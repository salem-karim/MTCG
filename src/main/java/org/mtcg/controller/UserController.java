package org.mtcg.controller;

import org.mtcg.dbaccess.UserDbAccess;
import org.mtcg.models.User;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.ContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserController {

  private final UserDbAccess userDbAccess;
  private final ObjectMapper objectMapper;

  public UserController() {
    this.userDbAccess = new UserDbAccess();
    this.objectMapper = new ObjectMapper(); // Initialize Jackson ObjectMapper
  }

  // Method to add a user
  public HttpResponse addUser(HttpRequest request) {
    try {
      // Assuming the request body contains JSON with user details
      User user = objectMapper.readValue(request.getBody(), User.class);

      boolean added = userDbAccess.addUser(user);

      if (added) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON, "User created successfully");
      } else {
        return new HttpResponse(HttpStatus.CONFLICT, ContentType.JSON, "User already exists");
      }
    } catch (JsonProcessingException e) {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Invalid request format");
    }
  }

  // Method to get a user by username
  public HttpResponse getUser(HttpRequest request) throws JsonProcessingException {
    // Extracting the username from path segments
    if (request.getPathSegments().size() < 2) {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Username not provided");
    }

    String username = request.getPathSegments().get(1); // Get the username from the path

    User user = userDbAccess.getUserByUsername(username);
    if (user != null) {
      return new HttpResponse(HttpStatus.OK, ContentType.JSON, objectMapper.writeValueAsString(user));
    } else {
      return new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON, "User not found");
    }
  }

  public HttpResponse loginUser(HttpRequest request) {
    try {
      User user = objectMapper.readValue(request.getBody(), User.class);
      String token = userDbAccess.getUserByUsername(user.getUsername()).getToken();
      if (token != null) {
        return new HttpResponse(HttpStatus.OK, ContentType.JSON, objectMapper.writeValueAsString(user));
      } else {
        return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Wrong login credentials");
      }
    } catch (JsonProcessingException e) {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Invalid request format");
    }
  }
}
