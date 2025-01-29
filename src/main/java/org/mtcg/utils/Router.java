package org.mtcg.utils;

import org.mtcg.services.Service;

import java.util.HashMap;
import java.util.Map;

public class Router implements IRouter {
  private final Map<String, Service> services = new HashMap<>();

  @Override
  public void addService(final String route, final Service service) {
    this.services.put(route, service);
  }

  @Override
  public void removeService(final String route) {
    this.services.remove(route);
  }

  @Override
  public Service resolve(final String route) {
    return this.services.get(route);
  }
}
