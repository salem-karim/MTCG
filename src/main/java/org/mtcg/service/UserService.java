package org.mtcg.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.mtcg.controller.UserController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.Method;
import org.mtcg.utils.Service;

import java.util.HashMap;

public class UserService implements Service {
  private final static Service DEFAULT_SERVICE = (HttpRequest req)-> new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "");
  private final HashMap<Method, Service> userMethods = new HashMap<>();
  private final UserController userController;

  public UserService() {
    this.userController = new UserController();
    userMethods.put(Method.GET, (HttpRequest req) -> {
      try {
        return this.userController.getUser(req);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });
    userMethods.put(Method.POST, this.userController::addUser);
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    Service service = userMethods.getOrDefault(request.getMethod(), UserService.DEFAULT_SERVICE);
    return service.handle(request);
  }
}
