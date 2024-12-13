package org.mtcg.controllers;

import java.sql.SQLException;

import org.mtcg.db.TransactionDbAccess;
import org.mtcg.db.UserDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.exceptions.HttpRequestException;

public class TransactionController extends Controller {
  private final TransactionDbAccess transactionDbAccess;
  private final UserDbAccess userDbAccess;

  public TransactionController() {
    super();
    this.transactionDbAccess = new TransactionDbAccess();
    this.userDbAccess = new UserDbAccess();
  }

  public HttpResponse buyPackage(final HttpRequest request) {
    try {
      // Step 1: Get user from token
      User user = userDbAccess.getUserFromToken(request.getHeaders());

      // Step 2: Check user's coin balance
      if (user.getCoins() < 5) {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
            "Insufficient funds");
      }

      // Step 3: Attempt to buy package
      boolean success = transactionDbAccess.buyPackage(user.getId());
      if (success) {
        user.setCoins(user.getCoins() - 5);
        userDbAccess.updateUserCoins(user);
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON, "Package purchased successfully");
      } else {
        return new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON, "No packages available");
      }

    } catch (HttpRequestException e) {
      // Catch exception thrown if authorization fails
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          "Unauthorized");
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Internal Server Error");
    }
  }
}
