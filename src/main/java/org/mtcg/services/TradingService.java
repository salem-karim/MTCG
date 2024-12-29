package org.mtcg.services;

import org.mtcg.controllers.TradingController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class TradingService extends DefaultService {

  public TradingService() {
    final var tradingController = new TradingController();
    super.methods.put(Method.POST, tradingController::createDeal);
    super.methods.put(Method.GET, tradingController::listDeals);
    super.methods.put(Method.DELETE, tradingController::deleteDeal);
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(), super::defaultResponse);
    return service.handle(request);
  }

}
