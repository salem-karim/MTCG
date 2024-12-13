package org.mtcg;

import org.mtcg.httpserver.HttpServer;
import org.mtcg.services.PackageService;
import org.mtcg.services.SessionService;
import org.mtcg.services.TransactionService;
import org.mtcg.services.UserService;
import org.mtcg.utils.Router;

public class Main {
  public static void main(String[] args) {
    var Http = new HttpServer(10001, configureRouter());
    Http.run();
  }

  // TODO: Make the Router Dynamic
  private static Router configureRouter() {
    Router router = new Router();
    router.addService("/users", new UserService());
    // router.addService("/users/{username}", new UserService());
    router.addService("/sessions", new SessionService());
    router.addService("/packages", new PackageService());
    router.addService("/transactions/packages", new TransactionService());
    //
    // router.addService("/cards", new CardService());
    // Also has routers like /deck?format=plain
    // router.addService("/deck", new DeckService());
    // router.addService("/stats", new StatService);
    // router.addService("/scoreboard", new ScoreBoardService());
    // router.addService("/battles", new BattleService());
    // router.addService("/tradings", new TradingService());
    // router.addService("/tradings/{UUID}", new TradingService());
    return router;
  }
}
