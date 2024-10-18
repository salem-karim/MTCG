package org.mtcg.service;

import org.mtcg.controller.UserController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.Method;

import java.util.HashMap;

public class UserService implements Service {
  private final static Service DEFAULT_SERVICE = (HttpRequest req)-> new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "");
  private final HashMap<Method, Service> userMethods = new HashMap<>();

  public UserService() {
    UserController userController = new UserController();
//    userMethods.put(Method.GET, (HttpRequest req) -> {
//      try {
//        return this.userController.getUser(req);
//      } catch (JsonProcessingException e) {
//        throw new RuntimeException(e);
//      }
//    });
    userMethods.put(Method.POST, userController::addUser);
  }

  @Override
  public HttpResponse handle(HttpRequest request) {
    Service service = userMethods.getOrDefault(request.getMethod(), UserService.DEFAULT_SERVICE);
    return service.handle(request);
  }
}
