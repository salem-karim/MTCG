package org.mtcg.services;

import org.mtcg.controllers.TradingController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class TradingService extends DefaultService {

  // Same as User Service
  public TradingService() {
    final var tradingController = new TradingController();
    super.methods.put(Method.GET, tradingController::listDeals);
    super.methods.put(Method.DELETE, tradingController::deleteDeal);
    // Since both paths "/tradings" and "/tradings/{UUID}" have a post Method decide
    // from the service route which Controller Method to call
    super.methods.put(Method.POST, (final HttpRequest req) -> {
      if (req.getServiceRoute().contains("/tradings/")) {
        return tradingController.trade(req);
      } else {
        return tradingController.createDeal(req);
      }
    });
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(), super::defaultResponse);
    return service.handle(request);
  }

}
