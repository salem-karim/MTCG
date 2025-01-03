package org.mtcg.services;

import org.mtcg.controllers.DeckController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class DeckService extends DefaultService {

  // Same as User Service
  public DeckService() {
    final var deckController = new DeckController();
    super.methods.put(Method.GET, deckController::getDeck);
    super.methods.put(Method.PUT, deckController::configureDeck);
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(), super::defaultResponse);
    return service.handle(request);
  }
}
