package org.mtcg.controller;

import org.mtcg.dbaccess.UserDbAccess;
import org.mtcg.models.User;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.ContentType;
import com.fasterxml.jackson.core.JsonProcessingException;

public class UserController extends Controller {
  private final UserDbAccess userDbAccess;

  public UserController() {
    super();
    this.userDbAccess = new UserDbAccess();
  }

  // Method to add a user
  public HttpResponse addUser(HttpRequest request) {
    try {
      // Assuming the request body contains JSON with user details
      User user = getObjectMapper().readValue(request.getBody(), User.class);
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

  // Updated loginUser method
  public HttpResponse loginUser(HttpRequest request) {
    try {
      // Get the raw request body
      String body = request.getBody();

      // Extracting username and password from the request body
      String username = body.split("\"Username\":\"")[1].split("\"")[0];
      String providedPassword = body.split("\"Password\":\"")[1].split("\"")[0];

      // Step 1: Retrieve the user from the database by username
      User userFromDb = userDbAccess.getUserByUsername(username); // This method should return a User with hashed password

      // Step 2: Verify password if user exists
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
