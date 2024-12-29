package org.mtcg.services;

import org.mtcg.controllers.SessionController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class SessionService extends DefaultService {
  public SessionService() {
    final var sessionController = new SessionController();
    methods.put(Method.POST, sessionController::loginUser); // Handle login
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(), this::defaultResponse);
    return service.handle(request);
  }
}
