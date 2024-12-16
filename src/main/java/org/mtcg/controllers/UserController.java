package org.mtcg.controllers;

import java.sql.SQLException;

import org.mtcg.db.UserDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

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
      final User user = getObjectMapper().readValue(request.getBody(), User.class);
      final boolean added = userDbAccess.addUser(user);
      // Handle Errors
      if (added) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON,
            createJsonMessage("message", "User created successfully"));
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
            createJsonMessage("error", "User already exists"));
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
}
