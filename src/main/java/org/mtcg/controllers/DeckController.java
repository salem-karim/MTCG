package org.mtcg.controllers;

import java.sql.SQLException;
import java.util.UUID;

import org.mtcg.db.DeckDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Deck;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.HttpRequestException;

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

      // get the users DeckId and the cards in it
      final UUID deckId = deckDbAccess.getDeckId(request.getUser().getId());
      final Deck deck = deckDbAccess.getDeckCards(deckId);
      if (deck == null) {
        return new HttpResponse(HttpStatus.NO_CONTENT, ContentType.JSON, "[ ]");
        // only continue if the deck has correct amount of cards
      } else if (deck.getCards().size() == 4) {
        // either format deck cards in plain text or in JSON
        if (request.getPath().contains("format=plain")) {
          StringBuilder cardString = new StringBuilder();
          for (final var card : deck.getCards()) {
            cardString.append(card.toString());
          }
          return new HttpResponse(HttpStatus.OK, ContentType.PLAIN_TEXT, cardString.toString());
        } else {
          try {
            // Serialize the cards array to JSON
            final String cardJSON = objectMapper.writeValueAsString(deck.getCards());

            // Now return the HTTP response with the serialized JSON
            return new HttpResponse(HttpStatus.OK, ContentType.JSON, cardJSON);

          } catch (final JsonProcessingException e) {
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
          createJsonMessage("error", "Access token is missing or invalid"));
    }
  }

  public HttpResponse configureDeck(final HttpRequest request) {
    try {
      if (request.getUser() == null) {
        throw new HttpRequestException("User not Authorized");
      }
      final UUID[] cardIds = getObjectMapper().readValue(request.getBody(), UUID[].class);
      if (cardIds.length != 4) {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
            createJsonMessage("error", "The provided deck did not include the required amount of cards"));
      }
      final UUID deckId = deckDbAccess.getDeckId(request.getUser().getId());

      if (deckDbAccess.configureDeck(deckId, cardIds, request.getUser().getId())) {
        return new HttpResponse(HttpStatus.OK, ContentType.JSON,
            createJsonMessage("message", "The deck has been successfully configured"));
      } else {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
            createJsonMessage("error", "Bad Request"));
      }
    } catch (final HttpRequestException e) {
      // Access was UNAUTHORIZED
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));

    } catch (final SQLException e) {
      System.out.println(e.getMessage());
      if (e.getMessage().contains("Conflict")) {
        // Conflict Error in DB
        return new HttpResponse(HttpStatus.CONFLICT, ContentType.JSON,
            createJsonMessage("error", "Deck is already configured"));

      } else if (e.getMessage()
          .contains("One of the cards is either not in the user's stack or is part of a trading deal.")) {
        // Custom Exception if the Users Stack does not include the cards
        return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON, createJsonMessage("error",
            "At least one of the provided cards does not belong to the user or is not available."));
      } else {
        // Should not happen so INTERNAL_SERVER_ERROR
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Something went wrong with the Database"));
      }
    } catch (final JsonProcessingException e) {
      // Malformed JSON in the Body
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid request format"));
    }
  }
}
