package org.mtcg.controllers;

import java.sql.SQLException;

import org.mtcg.db.UserDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.User;
import org.mtcg.models.UserData;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.HttpRequestException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class UserController extends Controller {
  private final UserDbAccess userDbAccess;

  public UserController() {
    super();
    this.userDbAccess = new UserDbAccess();
  }

  public HttpResponse addUser(final HttpRequest request) {
    try {
      // Make the User using the ObjectMapper which gets the JSON and uses the correct
      // Constructor annotated as JsonCreator
      final var user = getObjectMapper().readValue(request.getBody(), User.class);
      if (userDbAccess.addUser(user)) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON,
            createJsonMessage("message", "User successfully created"));
      } else {
        // This should never happen, as the addUser method in UserDbAccess
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Failed to add user"));
      }
    } catch (final SQLException e) {
      System.out.println(e.getMessage());
      if (e.getMessage().contains("Conflict")) {
        return new HttpResponse(HttpStatus.CONFLICT, ContentType.JSON,
            createJsonMessage("error", "User with same username already registered"));
      } else {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Failed to add user"));
      }
    } catch (final JsonProcessingException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid request format"));
    }
  }

  public HttpResponse listUser(final HttpRequest request) {
    try {
      // gets user from requests Authentication header and username from path
      final var reqUser = request.getUser();
      final String username = request.getPathSegments().get(1);

      // If unauthorized or request username and username dont match or isn't admin
      // respond with UNAUTHORIZED
      if (reqUser == null) {
        throw new HttpRequestException("Access token is missing or invalid");
      } else if (!reqUser.getUsername().equals(username) && !reqUser.getUsername().equals("admin")) {
        throw new HttpRequestException("Access token is missing or invalid");
      }
      // else get user from DB and respond with UserData in JSON
      final var user = userDbAccess.getUserByUsername(username);
      if (user == null) {
        return new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON, createJsonMessage("error", "User not Found"));
      } else {
        try {
          final var userData = new UserData(user.getUsername(), user.getBio(), user.getImage());
          return new HttpResponse(HttpStatus.OK, ContentType.JSON,
              getObjectMapper().writeValueAsString(userData));
        } catch (final JsonProcessingException e) {
          return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
              createJsonMessage("error", "Error serializing User Data."));
        }
      }
    } catch (final HttpRequestException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    }
  }

  public HttpResponse updateUser(final HttpRequest request) {
    // Same as listUser but instead does a UPDATE QUERY and responds with OK
    try {
      final var reqUser = request.getUser();
      final String username = request.getPathSegments().get(1);
      if (reqUser == null) {
        throw new HttpRequestException("Access token is missing or invalid");
      } else if (!reqUser.getUsername().equals(username) && !reqUser.getUsername().equals("admin")) {
        throw new HttpRequestException("Access token is missing or invalid");
      }
      final var userData = getObjectMapper().readValue(request.getBody(), UserData.class);
      final boolean success = userDbAccess.updateUserData(userData, username);
      if (success) {
        return new HttpResponse(HttpStatus.OK, ContentType.JSON,
            createJsonMessage("message", "User successfully updated"));
      } else {
        return new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON,
            createJsonMessage("error", "User not found"));
      }

    } catch (final HttpRequestException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    } catch (final JsonProcessingException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid request format"));
    }
  }
}
