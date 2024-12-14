package org.mtcg.controllers;

import java.util.ArrayList;

import org.mtcg.db.CardDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Card;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

public class CardController {
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
      String cardsString = "\n";
      for (Card card : cards) {
        cardsString += card.getName() + '\n';
      }
      return new HttpResponse(HttpStatus.OK, ContentType.JSON, cardsString + '\n');
    } else {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Bad Request\n");
    }
  }
}
