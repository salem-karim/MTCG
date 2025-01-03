package org.mtcg.controllers;

import org.mtcg.db.UserDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.LoginCredentials;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SessionController extends Controller {
  private final UserDbAccess userDbAccess;

  public SessionController() {
    super();
    this.userDbAccess = new UserDbAccess();
  }

  public HttpResponse loginUser(final HttpRequest request) {
    try {
      // Make LoginCredentials Object from Request Body
      final var loginCredentials = getObjectMapper().readValue(request.getBody(), LoginCredentials.class);
      final User userFromDb = userDbAccess.getUserByUsername(loginCredentials.getUsername());

      // Get User from the DB and verify passwords && Handle possible Errors
      if (userFromDb != null && userFromDb.verifyPassword(loginCredentials.getPassword())) {
        return new HttpResponse(HttpStatus.OK, ContentType.JSON,
            createJsonMessage("token", userFromDb.getToken()));
      } else {
        return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
            createJsonMessage("error", "Invalid username/password provided"));
      }
    } catch (final JsonProcessingException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid request format"));
    }
  }
}
