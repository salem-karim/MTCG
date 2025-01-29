package org.mtcg.controllers;

import org.mtcg.db.TransactionDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.HttpRequestException;

import java.util.UUID;

public class TransactionController extends Controller {
  private final TransactionDbAccess transactionDbAccess;

  public TransactionController() {
    this(new TransactionDbAccess());
  }

  public TransactionController(final TransactionDbAccess db) {
    super();
    this.transactionDbAccess = db;
  }

  public HttpResponse buyPackage(final HttpRequest request) {
    try {
      // Get user from token
      final User user = request.getUser();
      if (user == null) {
        throw new HttpRequestException("User not Authorized");
      }

      // Check user's coin balance for enough coins
      if (user.getCoins() < 5) {
        return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON,
            createJsonMessage("error", "Not enough money for buying a card package"));
      }

      try {
        // Retrieve following data to complete the DB transaction
        // Package, card data from DB and users Stack ID
        final UUID packageId = transactionDbAccess.getRandomPackage();
        // If Package Id is null it means there are not Packages in the DB and admin
        // needs to create more Packages
        if (packageId == null) {
          return new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON,
              createJsonMessage("error", "No card packages available for buying"));
        }

        final UUID[] cardIds = transactionDbAccess.getPackageCards(packageId);
        // If object is null or the wasn't filled respond with Server Error
        if (cardIds == null || cardIds.length == 0) {
          return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
              createJsonMessage("error", "Failed to retrieve package cards"));
        }

        final UUID stackId = transactionDbAccess.getStackId(user.getId());
        // Same here with StackId
        if (stackId == null) {
          return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
              createJsonMessage("error", "User does not have a valid stack"));
        }

        // set the Users coins to be updated when Package has been bought successfully
        user.setCoins(user.getCoins() - 5);

        // Perform the transaction (using a single connection)
        transactionDbAccess.performTransaction(user, packageId, cardIds, stackId);
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
