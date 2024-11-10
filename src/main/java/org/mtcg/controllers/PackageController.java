package org.mtcg.controllers;

import java.util.UUID;

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

  public PackageController() {
    super();
    this.pkgDbAccess = new PackageDbAccess();
  }

  public HttpResponse addPackage(final HttpRequest request) {
    try {
      // get userId form the DN using the token in the header
      final UUID userId = pkgDbAccess.getUserId(request.getHeaders());
      // Construct the cards from the request body
      final Card[] cards = getObjectMapper().readValue(request.getBody(), Card[].class);
      // Make the Package object
      final var pkg = new Package(cards, userId);
      final boolean added = pkgDbAccess.addPackage(pkg);
      if (added) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON, "Package created successfully\n");
      } else {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Bad Request\n");
      } // General Error handling for malformed JSON
    } catch (final JsonProcessingException e) {
      System.out.println(e);
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Invalid request format\n");
      // Handle Error for Construction of Oackage Object
    } catch (final IllegalArgumentException e) {
      System.out.println(e);
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Not 5 cards in Request Body\n");
      // Handle Error for wwrong Authorization
    } catch (final HttpRequestException e) {
      System.out.println(e);
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          "Authorization header is missing or invalid\n");
    }
  }
}
