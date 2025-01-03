package org.mtcg.services;

import org.mtcg.controllers.ScoreBoardController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class ScoreBoardService extends DefaultService {

  // Same as User Service
  public ScoreBoardService() {
    final var scoreBoardController = new ScoreBoardController();
    super.methods.put(Method.GET, scoreBoardController::listScoreBoard);
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(), super::defaultResponse);
    return service.handle(request);
  }

}
