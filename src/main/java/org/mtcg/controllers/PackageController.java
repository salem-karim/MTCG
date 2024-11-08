package org.mtcg.controllers;

import org.mtcg.db.PackageDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Card;
import org.mtcg.models.Package;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PackageController extends Controller {
  private final PackageDbAccess pkgDbAccess;

  public PackageController() {
    super();
    this.pkgDbAccess = new PackageDbAccess();
  }

  public HttpResponse addPackage(final HttpRequest request) {
    try {
      final Card[] cards = getObjectMapper().readValue(request.getBody(), Card[].class);
      final var pkg = new Package(cards);
      final boolean added = pkgDbAccess.addPackage(pkg);
      if (added) {
        return new HttpResponse(HttpStatus.OK, ContentType.JSON, "Package created successfully\n");
      } else {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Bad Request\n");
      }
    } catch (final JsonProcessingException e) {
      System.out.println(e);
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Invalid request format\n");
    } catch (final IllegalArgumentException e) {
      System.out.println(e);
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Not 5 cards in Request Body\n");
    }
  }
}
