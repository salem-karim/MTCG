package org.mtcg.services;

import org.mtcg.controllers.PackageController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.Method;

public class PackageService extends DefaultService {

  public PackageService() {
    final var packageController = new PackageController();
    methods.put(Method.GET, packageController::addPackage);
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = methods.getOrDefault(request.getMethod(), this::defaultResponse);
    return service.handle(request);
  }
}
