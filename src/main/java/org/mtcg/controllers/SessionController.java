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
      final var user = getObjectMapper().readValue(request.getBody(), LoginCredentials.class);
      final User userFromDb = userDbAccess.getUserByUsername(user.getUsername());
      if (userFromDb != null && userFromDb.verifyPassword(user.getPassword())) {
        return new HttpResponse(HttpStatus.OK, ContentType.JSON, userFromDb.getToken() + '\n');
      } else {
        return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Wrong login credentials\n");
      }
    } catch (final JsonProcessingException e) {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Invalid request format\n");
    }
  }
}
