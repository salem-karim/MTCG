package org.mtcg.utils;

import org.mtcg.service.Service;

import java.util.HashMap;
import java.util.Map;

public class Router implements IRouter {
  private final Map<String, Service> services = new HashMap<>();

  @Override
  public void addService(String route, Service service) {
    this.services.put(route, service);
  }

  @Override
  public void removeService(String route) {
    this.services.remove(route);
  }

  @Override
  public Service resolve(String route) {
    return this.services.get(route);
  }
}
