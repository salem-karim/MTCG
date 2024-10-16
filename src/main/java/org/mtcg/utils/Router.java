package org.mtcg.utils;

import java.util.HashMap;
import java.util.Map;

public class Router {
  private Map<String, Service> services = new HashMap<>();

  public void addService(String route, Service service) {
    this.services.put(route, service);
  }

  public void removeService(String route) {
    this.services.remove(route);
  }

  public Service resolve(String route) {
    return this.services.get(route);
  }
}
