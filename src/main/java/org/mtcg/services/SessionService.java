package org.mtcg.services;

import org.mtcg.controllers.SessionController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.Method;

import java.util.HashMap;

public class SessionService implements Service {
  private final static Service DEFAULT_SERVICE = (final HttpRequest req) -> new HttpResponse(HttpStatus.BAD_REQUEST,
      ContentType.JSON, "");
  private final HashMap<Method, Service> sessionMethods = new HashMap<>();

  public SessionService() {
    final SessionController sessionController = new SessionController();
    sessionMethods.put(Method.POST, sessionController::loginUser); // Handle login
    // sessionMethods.put(Method.DELETE, sessionController::logoutUser); // Handle
    // logout
    // sessionMethods.put(Method.GET, sessionController::checkSession); // Check
    // session status
  }

  @Override
  public HttpResponse handle(final HttpRequest request) {
    final Service service = sessionMethods.getOrDefault(request.getMethod(), SessionService.DEFAULT_SERVICE);
    return service.handle(request);
  }
}
