package org.mtcg.services;

import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;

@FunctionalInterface
public interface Service {
  HttpResponse handle(final HttpRequest request);
}
