package org.mtcg.controllers;

import org.mtcg.db.PackageDbAccess;
import org.mtcg.db.UserDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Card;
import org.mtcg.models.Package;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.exceptions.HttpRequestException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PackageController extends Controller {
  private final PackageDbAccess pkgDbAccess;
  private final UserDbAccess userDbAccess;

  public PackageController() {
    this(new PackageDbAccess(), new UserDbAccess());
  }

  // Constructor for dependency injection
  public PackageController(PackageDbAccess pkgDbAccess, UserDbAccess userDbAccess) {
    super();
    this.pkgDbAccess = pkgDbAccess;
    this.userDbAccess = userDbAccess;
  }

  public HttpResponse addPackage(final HttpRequest request) {
    try {
      // Get user from token
      final User user = userDbAccess.getUserFromToken(request.getHeaders());

      // Construct the cards from the request body
      final Card[] cards = getObjectMapper().readValue(request.getBody(), Card[].class);

      // Make the Package object
      final var pkg = new Package(cards, user.getId());
      final boolean added = pkgDbAccess.addPackage(pkg);

      if (added) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON, "Package created successfully\n");
      } else {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Bad Request\n");
      }

    } catch (final JsonProcessingException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Invalid request format\n");

    } catch (final IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Not 5 cards in Request Body\n");

    } catch (final HttpRequestException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          "Authorization header is missing or invalid\n");
    }
  }

}
