package org.mtcg.services;

import org.mtcg.controllers.UserController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class UserService extends DefaultService {

  // Map the Methods to Controller Methods which have the same signature as a
  // Service meaning returns HttpResponse and has HttpRequest as parameter
  public UserService() {
    final var userController = new UserController();
    super.methods.put(Method.POST, userController::addUser);
    super.methods.put(Method.GET, userController::listUser);
    super.methods.put(Method.PUT, userController::updateUser);
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(), super::defaultResponse);
    return service.handle(request);
  }
}
