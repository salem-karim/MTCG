package org.mtcg.controllers;

import org.mtcg.db.TransactionDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.exceptions.HttpRequestException;

public class TransactionController extends Controller {
  private final TransactionDbAccess transactionDbAccess;

  public TransactionController() {
    super();
    this.transactionDbAccess = new TransactionDbAccess();
  }

  public HttpResponse buyPackage(final HttpRequest request) {
    try {
      // Step 1: Get user from token
      final User user = request.getUser();
      if (user == null) {
        throw new HttpRequestException("User not Authorized");
      }

      // Step 2: Check user's coin balance
      if (user.getCoins() < 5) {
        return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON,
            createJsonMessage("error", "Not enough money for buying a card package"));
      }
      user.setCoins(user.getCoins() - 5);
      // Step 3: Attempt to buy package
      // TODO: move Logic from TransactionDbAccess to here
      if (transactionDbAccess.buyPackage(user)) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON,
            createJsonMessage("message", "Package purchased successfully"));
      } else {
        return new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON,
            createJsonMessage("error", "No card packages available for buying"));
      }

    } catch (final HttpRequestException e) {
      // Catch exception thrown if authorization fails
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    }
  }
}
