package org.mtcg.controllers;

//import org.mtcg.db.DeckDbAccess;
//import org.mtcg.db.StackDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

public class BattleController extends Controller {
  // private final DeckDbAccess deckDbAccess;
  // private final StackDbAccess stackDbAccess;
  //
  // public BattleController() {
  // super();
  // this.deckDbAccess = new DeckDbAccess();
  // this.stackDbAccess = new StackDbAccess();
  // }

  public HttpResponse battle(final HttpRequest request) {
    return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Bad Request");
  }

}
