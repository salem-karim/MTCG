package org.mtcg.controllers;

import java.util.concurrent.ExecutionException;

import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.battle.BattleLobby;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

public class BattleController extends Controller {
  private final BattleLobby battleLobby;

  public BattleController() {
    super();
    this.battleLobby = BattleLobby.getInstance();
  }

  public HttpResponse battle(final HttpRequest request) {
    final var user = request.getUser();
    if (user == null) {
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    }
    try {
      // Battle Lobby adds user to list and
      // if 2 users are found battle starts and returns battle Log
      final String battleLog = battleLobby.addUserToLobby(user).get();
      return new HttpResponse(HttpStatus.OK, ContentType.JSON, createJsonMessage("message", battleLog));
      // catch Error because of asynchronous Method
    } catch (ExecutionException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Getting the battle log failed"));
    } catch (final NullPointerException e) {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", e.getMessage()));
    } catch (Exception e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
          createJsonMessage("error", "failed to execute the battle"));
    }

  }

}
