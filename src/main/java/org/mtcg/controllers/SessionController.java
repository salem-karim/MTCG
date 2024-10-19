package org.mtcg.controllers;

import org.mtcg.db.UserDbAccess;
import org.mtcg.models.User;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

public class SessionController extends Controller {
  private final UserDbAccess userDbAccess;

  public SessionController() {
    super();
    this.userDbAccess = new UserDbAccess();
  }

  public HttpResponse loginUser(HttpRequest request) {
    try {
      String body = request.getBody();
      String username = body.split("\"Username\":\"")[1].split("\"")[0];
      String providedPassword = body.split("\"Password\":\"")[1].split("\"")[0];

      User userFromDb = userDbAccess.getUserByUsername(username);

      if (userFromDb != null && userFromDb.verifyPassword(providedPassword)) {
        return new HttpResponse(HttpStatus.OK, ContentType.JSON, userFromDb.getToken());
      } else {
        return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON, "Wrong login credentials");
      }
    } catch (ArrayIndexOutOfBoundsException | NullPointerException e) {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Invalid request format");
    }
  }
}
