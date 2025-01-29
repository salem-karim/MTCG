package org.mtcg.services;

import org.mtcg.controllers.TransactionController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class TransactionService extends DefaultService {

  // Same as User Service
  public TransactionService() {
    final var transactionController = new TransactionController();

    methods.put(Method.POST, transactionController::buyPackage);
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(),
        this::defaultResponse);
    return service.handle(request);
  }

}
