package org.mtcg.controllers;

import java.sql.SQLException;
import java.util.UUID;

import org.mtcg.db.DeckDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Card;
import org.mtcg.models.Deck;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.exceptions.HttpRequestException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class DeckController extends Controller {
  private final DeckDbAccess deckDbAccess;

  public DeckController() {
    super();
    this.deckDbAccess = new DeckDbAccess();
  }

  public HttpResponse getDeck(final HttpRequest request) {
    try {
      if (request.getUser() == null) {
        throw new HttpRequestException("User not Authorized");
      }

      final UUID deckId = deckDbAccess.getDeckId(request.getUser().getId());
      final Deck deck = deckDbAccess.getDeckCards(deckId);
      if (deck == null) {
        return new HttpResponse(HttpStatus.OK, ContentType.JSON, "");
      } else if (deck.getCards().length == 4) {
        if (request.getPath().contains("format=plain")) {
          String cardString = "";
          for (final var card : deck.getCards()) {
            cardString += "ID: " + card.getId() + " Name: " + card.getName() + "\nDamage: " + card.getDamage()
                + " Element/Type: " + card.getElement() + '/' + card.getCardType() + '\n';
          }
          return new HttpResponse(HttpStatus.OK, ContentType.PLAIN_TEXT, cardString);
        } else {
          try {
            // Serialize the cards array to JSON
            String cardJSON = objectMapper.writeValueAsString(deck.getCards());

            // Now return the HTTP response with the serialized JSON
            return new HttpResponse(HttpStatus.OK, ContentType.JSON, cardJSON);

          } catch (JsonProcessingException e) {
            System.out.println("Failed to serialize cards to JSON: " + e.getMessage());
            return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, "Error serializing cards.\n");
          }
        }
      } else {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Wrong Number of Cards in Deck\n");
      }
    } catch (final IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Wrong Number of Cards in Deck\n");
    } catch (final HttpRequestException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          "Authorization header is missing or invalid\n");
    }
  }

  public HttpResponse configureDeck(final HttpRequest request) {
    try {
      if (request.getUser() == null) {
        throw new HttpRequestException("User not Authorized");
      }
      final UUID[] cardIds = getObjectMapper().readValue(request.getBody(), UUID[].class);
      final UUID deckId = deckDbAccess.getDeckId(request.getUser().getId());
      boolean updated = deckDbAccess.configureDeck(deckId, cardIds);
      if (updated) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON, "Deck got configured\n");
      } else {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Bad Request\n");
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Not 4 cards in Request Body\n");

    } catch (final HttpRequestException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          "Authorization header is missing or invalid\n");

    } catch (final SQLException e) {
      System.out.println(e);
      return new HttpResponse(HttpStatus.CONFLICT, ContentType.JSON, "User already exists\n");

    } catch (final JsonProcessingException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "Invalid request format\n");
    }
  }
}
