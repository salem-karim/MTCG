package org.mtcg.controllers;

import java.sql.SQLException;

import org.mtcg.db.PackageDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Card;
import org.mtcg.models.Package;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.HttpRequestException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PackageController extends Controller {
  private final PackageDbAccess pkgDbAccess;

  public PackageController(final PackageDbAccess packageDbAccess) {
    super();
    this.pkgDbAccess = packageDbAccess;
  }

  public PackageController() {
    this(new PackageDbAccess());
  }

  public HttpResponse addPackage(final HttpRequest request) {
    try {
      // Get user from token
      if (request.getUser() == null) {
        throw new HttpRequestException("User not Authorized");
      }
      // Make sure the user is admin
      if (!request.getUser().getUsername().equals("admin")) {
        return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON,
            createJsonMessage("error", "Provided user is not \"admin\""));
      }

      // Construct the cards from the request body
      final Card[] cards = getObjectMapper().readValue(request.getBody(), Card[].class);
      if (cards.length != 5) {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
            createJsonMessage("error", "The provided package did not include the required amount of cards"));
      }

      // Make the Package object
      final var pkg = new Package(cards, request.getUser().getId());

      // if DB Method fails respond with Server Error
      if (pkgDbAccess.addPackage(pkg)) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON,
            createJsonMessage("message", "Package created successfully"));
      } else {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Failed to add Package"));
      }

      // Other Errors
    } catch (final JsonProcessingException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid request format"));

    } catch (final IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Not 5 cards in Request Body"));

    } catch (final HttpRequestException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    } catch (final SQLException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.CONFLICT, ContentType.JSON,
          createJsonMessage("error", "At least one card in the package already exists"));

    }
  }
}
