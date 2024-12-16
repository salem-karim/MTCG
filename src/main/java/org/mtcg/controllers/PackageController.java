package org.mtcg.controllers;

import org.mtcg.db.PackageDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Card;
import org.mtcg.models.Package;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.exceptions.HttpRequestException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PackageController extends Controller {
  private final PackageDbAccess pkgDbAccess;

  public PackageController(PackageDbAccess packageDbAccess) {
    this.pkgDbAccess = packageDbAccess;
  }

  public HttpResponse addPackage(final HttpRequest request) {
    try {
      // Get user from token
      if (request.getUser() == null) {
        throw new HttpRequestException("User not Authorized");
      }

      // Construct the cards from the request body
      final Card[] cards = getObjectMapper().readValue(request.getBody(), Card[].class);

      // Make the Package object
      final var pkg = new Package(cards, request.getUser().getId());
      final boolean added = pkgDbAccess.addPackage(pkg);

      if (added) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON,
            createJsonMessage("message", "Package created successfully"));
      } else {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
            createJsonMessage("error", "Bad Request"));
      }

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
          createJsonMessage("error", "Authorization header is missing or invalid"));
    }
  }
}
