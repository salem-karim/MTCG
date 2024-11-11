package org.mtcg.services;

import org.mtcg.controllers.UserController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class UserService extends DefaultService {

  public UserService() {
    final var userController = new UserController();
    // userMethods.put(Method.GET, (HttpRequest req) -> {
    // try {
    // return this.userController.getUser(req);
    // } catch (JsonProcessingException e) {
    // throw new RuntimeException(e);
    // }
    // });
    super.methods.put(Method.POST, userController::addUser);
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(), super::defaultResponse);
    return service.handle(request);
  }
}
