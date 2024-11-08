package org.mtcg.services;

import java.util.HashMap;

import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.Method;

public abstract class DefaultService implements Service {
  protected final HashMap<Method, Service> methods = new HashMap<>();

  // Default behavior for unhandled requests
  protected HttpResponse defaultResponse(HttpRequest request) {
    return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "");
  }

  // Optional: other shared methods for subclasses to use if needed
}
