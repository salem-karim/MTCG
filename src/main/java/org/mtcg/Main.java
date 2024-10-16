package org.mtcg;

import org.mtcg.httpserver.HttpServer;
import org.mtcg.service.UserService;
import org.mtcg.utils.Router;

public class Main {
  public static void main(String[] args) {
    var Http = new HttpServer(10001, configureRouter());
    Http.run();
  }
  private static Router configureRouter() {
    Router router = new Router();
    router.addService("/users", new UserService());
    return router;
  }
}