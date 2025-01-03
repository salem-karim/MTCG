package org.mtcg.services;

import org.mtcg.controllers.CardController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class CardService extends DefaultService {

  // Same as User Service
  public CardService() {
    final var cardController = new CardController();
    methods.put(Method.GET, cardController::listCards);
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(), this::defaultResponse);
    return service.handle(request);
  }
}
