package org.mtcg;

import org.mtcg.httpserver.HttpServer;
import org.mtcg.services.BattleService;
import org.mtcg.services.CardService;
import org.mtcg.services.DeckService;
import org.mtcg.services.PackageService;
import org.mtcg.services.ScoreBoardService;
import org.mtcg.services.SessionService;
import org.mtcg.services.StatService;
import org.mtcg.services.TradingService;
import org.mtcg.services.TransactionService;
import org.mtcg.services.UserService;
import org.mtcg.utils.Router;

public class Main {
  public static void main(String[] args) {
    var Http = new HttpServer(10001, configureRouter());
    Http.run();
  }

  private static Router configureRouter() {
    Router router = new Router();
    router.addService("/users", new UserService());
    router.addService("/users/", new UserService());
    router.addService("/sessions", new SessionService());
    router.addService("/packages", new PackageService());
    router.addService("/transactions/packages", new TransactionService());
    router.addService("/cards", new CardService());
    router.addService("/deck", new DeckService());
    router.addService("/deck?format=plain", new DeckService());
    router.addService("/stats", new StatService());
    router.addService("/scoreboard", new ScoreBoardService());
    router.addService("/battles", new BattleService());
    router.addService("/tradings", new TradingService());
    router.addService("/tradings/", new TradingService());
    return router;
  }
}
