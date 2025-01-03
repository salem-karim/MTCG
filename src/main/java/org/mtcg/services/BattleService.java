package org.mtcg.services;

import org.mtcg.controllers.BattleController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class BattleService extends DefaultService {

  // Same as User Service
  public BattleService() {
    final var battleController = new BattleController();
    super.methods.put(Method.POST, battleController::battle);
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(), super::defaultResponse);
    return service.handle(request);
  }

}
