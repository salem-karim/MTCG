package org.mtcg.controllers;

import java.sql.SQLException;

import org.mtcg.db.UserDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.User;
import org.mtcg.models.UserData;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.exceptions.HttpRequestException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class UserController extends Controller {
  private final UserDbAccess userDbAccess;

  public UserController() {
    super();
    this.userDbAccess = new UserDbAccess();
  }

  // Method to add a user
  public HttpResponse addUser(final HttpRequest request) {
    try {
      // Construct the User using the ObjectMapper which selects the correct
      // Constructor
      final var user = getObjectMapper().readValue(request.getBody(), User.class);
      final boolean added = userDbAccess.addUser(user);
      // Handle Errors
      if (added) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON,
            createJsonMessage("message", "User successfully created"));
      } else {
        // This should never happen, as the addUser method in UserDbAccess
        // should either succeed or throw an exception
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Failed to add user"));
      }

    } catch (final SQLException e) {
      System.out.println(e);
      if (e.getMessage().contains("Conflict")) {
        return new HttpResponse(HttpStatus.CONFLICT, ContentType.JSON,
            createJsonMessage("error", "User with same username already registered"));
      } else {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Failed to add user"));
      }
    } catch (final JsonProcessingException e) {
      System.out.println(e);
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid request format"));
    }
  }

  public HttpResponse listUser(final HttpRequest request) {
    try {
      final var reqUser = request.getUser();
      final String username = request.getPathSegments().get(1);
      if (reqUser == null) {
        throw new HttpRequestException("User not authorized");
      } else if (!reqUser.getUsername().equals(username) && !reqUser.getUsername().equals("admin")) {
        throw new HttpRequestException("User not authorized");
      }
      final var user = userDbAccess.getUserByUsername(username);
      if (user == null) {
        return new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON, createJsonMessage("error", "User not Found"));
      } else {
        try {
          final var userData = new UserData(user.getUsername(), user.getBio(), user.getImage());
          final String dataJSON = getObjectMapper().writeValueAsString(userData);
          return new HttpResponse(HttpStatus.OK, ContentType.JSON, dataJSON);
        } catch (final JsonProcessingException e) {
          return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
              createJsonMessage("error", "Error serializing User Data."));
        }
      }
    } catch (final HttpRequestException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Authorization header is missing or invalid"));
    }
  }

  public HttpResponse updateUser(final HttpRequest request) {
    try {
      final var reqUser = request.getUser();
      final String username = request.getPathSegments().get(1);
      if (reqUser == null) {
        throw new HttpRequestException("User not authorized");
      } else if (!reqUser.getUsername().equals(username) && !reqUser.getUsername().equals("admin")) {
        throw new HttpRequestException("User not authorized");
      }
      final var userData = getObjectMapper().readValue(request.getBody(), UserData.class);
      final boolean success = userDbAccess.updateUserData(userData, username);
      if (success) {
        return new HttpResponse(HttpStatus.OK, ContentType.JSON,
            createJsonMessage("message", "User successfully updated"));
      } else {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Failed to add user"));
      }

    } catch (final HttpRequestException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Authorization header is missing or invalid"));
    } catch (final JsonProcessingException e) {
      System.out.println(e);
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid request format"));
    }
  }
}
