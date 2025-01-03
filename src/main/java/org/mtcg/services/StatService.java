package org.mtcg.services;

import org.mtcg.controllers.StatController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class StatService extends DefaultService {

  // Same as User Service
  public StatService() {
    final var statController = new StatController();
    super.methods.put(Method.GET, statController::listUsersStats);
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(), super::defaultResponse);
    return service.handle(request);
  }

}
