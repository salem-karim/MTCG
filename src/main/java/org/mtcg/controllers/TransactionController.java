package org.mtcg.controllers;

import org.mtcg.db.TransactionDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.exceptions.HttpRequestException;

import java.util.UUID;

public class TransactionController extends Controller {
  private final TransactionDbAccess transactionDbAccess;

  public TransactionController() {
    super();
    this.transactionDbAccess = new TransactionDbAccess();
  }

  public HttpResponse buyPackage(final HttpRequest request) {
    try {
      // Step 1: Get user from token to check for auth
      final User user = request.getUser();
      if (user == null) {
        throw new HttpRequestException("User not Authorized");
      }

      // Step 2: Check user's coin balance for enough coins
      if (user.getCoins() < 5) {
        return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON,
            createJsonMessage("error", "Not enough money for buying a card package"));
      }

      try {
        // Step 3: Retrieve following data to complete the db transaction
        // Package, card data and users Stack ID
        final UUID packageId = transactionDbAccess.getRandomPackage();
        if (packageId == null) {
          return new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON,
              createJsonMessage("error", "No card packages available for buying"));
        }

        final UUID[] cardIds = transactionDbAccess.getPackageCards(packageId);
        if (cardIds == null || cardIds.length == 0) {
          return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
              createJsonMessage("error", "Failed to retrieve package cards"));
        }

        final UUID stackId = transactionDbAccess.getStackId(user.getId());
        if (stackId == null) {
          return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
              createJsonMessage("error", "User does not have a valid stack"));
        }

        user.setCoins(user.getCoins() - 5);

        // Step 4: Perform the transaction (using a single connection)
        transactionDbAccess.performTransaction(user, packageId, cardIds, stackId);

        // Deduct coins from the user object after successful transaction

        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON,
            createJsonMessage("message", "Package purchased successfully"));

      } catch (Exception e) {
        // Catch database exceptions during data retrieval or transaction execution
        System.err.println("Error during package purchase: " + e.getMessage());
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "An error occurred during package purchase"));
      }

    } catch (HttpRequestException e) {
      // Catch exception thrown if authorization fails
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    }
  }
}
