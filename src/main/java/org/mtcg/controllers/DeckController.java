package org.mtcg.controllers;

import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;

import org.mtcg.db.DeckDbAccess;
import org.mtcg.db.StackDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Deck;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.exceptions.HttpRequestException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class DeckController extends Controller {
  private final DeckDbAccess deckDbAccess;
  private final StackDbAccess stackDbAccess;

  public DeckController() {
    super();
    this.deckDbAccess = new DeckDbAccess();
    this.stackDbAccess = new StackDbAccess();
  }

  public HttpResponse getDeck(final HttpRequest request) {
    try {
      if (request.getUser() == null) {
        throw new HttpRequestException("User not Authorized");
      }

      final UUID deckId = deckDbAccess.getDeckId(request.getUser().getId());
      final Deck deck = deckDbAccess.getDeckCards(deckId);
      if (deck == null) {
        return new HttpResponse(HttpStatus.NO_CONTENT, ContentType.JSON, "[ ]");
      } else if (deck.getCards().length == 4) {
        if (request.getPath().contains("format=plain")) {
          String cardString = "";
          for (final var card : deck.getCards()) {
            cardString += card.toString();
          }
          return new HttpResponse(HttpStatus.OK, ContentType.PLAIN_TEXT, cardString);
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
      // get the needed Info to check for valid User Stack Cards in Body HashSet is
      // used for O(1) validation
      final UUID[] cardIds = getObjectMapper().readValue(request.getBody(), UUID[].class);
      final Set<UUID> userStackCards = stackDbAccess.getUserStack(request.getUser().getId());

      // check if the request has cards only of the users Stack to not configure a
      // Deck with other cards which the User has not purchased
      for (final UUID cardId : cardIds) {
        if (!userStackCards.contains(cardId)) {
          return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON, createJsonMessage("error",
              "At least one of the provided cards does not belong to the user or is not available."));
        }
      }
      final UUID deckId = deckDbAccess.getDeckId(request.getUser().getId());
      final boolean configure = deckDbAccess.configureDeck(deckId, cardIds);
      if (configure) {
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
      System.out.println(e);
      // Conflict Error in DB
      if (e.getMessage().contains("Conflict")) {
        return new HttpResponse(HttpStatus.CONFLICT, ContentType.JSON,
            createJsonMessage("error", "Deck is already configured"));

        // Custom Bad Request Error in DB if Body does not contain 4 cards
      } else if (e.getMessage().contains("A deck can only contain 4 cards")) {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
            createJsonMessage("error", "The provided deck did not include the required amount of cards"));
        // Should not happen so INTERNAL_SERVER_ERROR
      } else
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Something went wrong with the Database"));

    } catch (final JsonProcessingException e) {
      // Malformed JSON in the Body
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid request format"));
    }
  }
}
