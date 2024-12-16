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
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON, "User created successfully\n");
      } else {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Bad Request\n");
      }
    } catch (final JsonProcessingException e) {
      System.out.println(e);
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Invalid request format\n");
    } catch (final SQLException e) {
      System.out.println(e);
      return new HttpResponse(HttpStatus.CONFLICT, ContentType.JSON, "User already exists\n");
    }
  }
}
