package org.mtcg.controllers;

import java.sql.SQLException;
import java.util.UUID;

import org.mtcg.db.DeckDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
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
        return new HttpResponse(HttpStatus.OK, ContentType.JSON,
            createJsonMessage("message", "No deck found"));
      } else if (deck.getCards().length == 4) {
        if (request.getPath().contains("format=plain")) {
          StringBuilder cardString = new StringBuilder();
          for (final var card : deck.getCards()) {
            cardString.append("ID: ").append(card.getId())
                .append(" Name: ").append(card.getName())
                .append("\nDamage: ").append(card.getDamage())
                .append(" Element/Type: ").append(card.getElement())
                .append('/').append(card.getCardType())
                .append('\n');
          }
          return new HttpResponse(HttpStatus.OK, ContentType.PLAIN_TEXT, cardString.toString());
        } else {
          try {
            // Serialize the cards array to JSON
            String cardJSON = objectMapper.writeValueAsString(deck.getCards());

            // Now return the HTTP response with the serialized JSON
            return new HttpResponse(HttpStatus.OK, ContentType.JSON, cardJSON);

          } catch (JsonProcessingException e) {
            System.out.println("Failed to serialize cards to JSON: " + e.getMessage());
            return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
                createJsonMessage("error", "Error serializing cards"));
          }
        }
      } else {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
            createJsonMessage("error", "Wrong number of cards in deck"));
      }
    } catch (final IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Wrong number of cards in deck"));
    } catch (final HttpRequestException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Authorization header is missing or invalid"));
    }
  }

  public HttpResponse configureDeck(final HttpRequest request) {
    try {
      if (request.getUser() == null) {
        throw new HttpRequestException("User not Authorized");
      }
      final UUID[] cardIds = getObjectMapper().readValue(request.getBody(), UUID[].class);
      final UUID deckId = deckDbAccess.getDeckId(request.getUser().getId());
      boolean configure = deckDbAccess.configureDeck(deckId, cardIds);
      if (configure) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON,
            createJsonMessage("message", "Deck got configured successfully"));
      } else {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
            createJsonMessage("error", "Bad Request"));
      }
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Not 4 cards in request body"));

    } catch (final HttpRequestException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Authorization header is missing or invalid"));

    } catch (final SQLException e) {
      System.out.println(e);
      return new HttpResponse(HttpStatus.CONFLICT, ContentType.JSON,
          createJsonMessage("error", "User already exists"));

    } catch (final JsonProcessingException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid request format"));
    }
  }
}
