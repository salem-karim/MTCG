package org.mtcg.utils;

import org.mtcg.service.Service;

public interface IRouter {
  void addService(String route, Service service);

  void removeService(String route);

  Service resolve(String route);
}
