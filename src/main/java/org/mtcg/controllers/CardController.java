package org.mtcg.controllers;

import java.util.ArrayList;

import org.mtcg.db.CardDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Card;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

public class CardController extends Controller {
  private final CardDbAccess cardDbAccess;

  public CardController(CardDbAccess cardDbAccess) {
    this.cardDbAccess = cardDbAccess;
  }

  public HttpResponse listCards(final HttpRequest request) {
    if (request.getUser() == null) {
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          "Authorization header is missing or invalid\n");
    }
    final ArrayList<Card> cards = cardDbAccess.getCards(request.getUser().getId());
    if (cards != null) {
      try {
        // Serialize the cards array to JSON
        String cardsJSON = getObjectMapper().writeValueAsString(cards);

        // Return the serialized JSON string
        return new HttpResponse(HttpStatus.OK, ContentType.JSON, cardsJSON);

      } catch (JsonProcessingException e) {
        System.out.println("Failed to serialize cards to JSON: " + e.getMessage());
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Error serializing cards.");
      }
    } else {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Bad Request\n");
    }
  }
}
