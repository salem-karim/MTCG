package org.mtcg.utils;

import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;

public interface Service {
  HttpResponse handle(HttpRequest request);
}
