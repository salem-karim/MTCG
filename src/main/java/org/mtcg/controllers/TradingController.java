package org.mtcg.controllers;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.mtcg.db.CardDbAccess;
import org.mtcg.db.StackDbAccess;
import org.mtcg.db.TradingDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Trade;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TradingController extends Controller {
  private final TradingDbAccess tradingDbAccess;
  private final StackDbAccess stackDbAccess;
  private final CardDbAccess cardDbAccess;

  public TradingController() {
    super();
    this.tradingDbAccess = new TradingDbAccess();
    this.stackDbAccess = new StackDbAccess();
    this.cardDbAccess = new CardDbAccess();
  }

  public HttpResponse listDeals(final HttpRequest request) {
    if (request.getUser() == null) {
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    } else {
      final List<Trade> trades = tradingDbAccess.getTradingDeals();
      if (trades == null) {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Error while retrieving all User Stats"));
      } else if (trades.isEmpty()) {
        return new HttpResponse(HttpStatus.NO_CONTENT, ContentType.JSON, "\n");
      } else {
        try {
          return new HttpResponse(HttpStatus.OK, ContentType.JSON,
              getObjectMapper().writeValueAsString(trades));
        } catch (final JsonProcessingException e) {
          System.out.println(e.getMessage());
          return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
              createJsonMessage("error", "Serialisation Error"));
        }
      }
    }
  }

  public HttpResponse createDeal(final HttpRequest request) {
    if (request.getUser() == null) {
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    } else {
      try {
        final var partialTrade = getObjectMapper().readValue(request.getBody(), Trade.class);

        final var trade = partialTrade.withUserId(request.getUser().getId());
        final Set<UUID> userStackCards = stackDbAccess.getUserStack(request.getUser().getId());

        // check if the requests card is in Users Stack
        if (!userStackCards.contains(trade.getCardId())) {
          return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON, createJsonMessage("error",
              "At least one of the provided cards does not belong to the user or is not available."));
        }

        tradingDbAccess.insertDeal(trade);
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON, "\n");
      } catch (JsonProcessingException e) {
        System.out.println(e.getMessage());
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
            createJsonMessage("error", "Serialisation Error"));
      } catch (SQLException e) {
        System.out.println(e.getMessage());
        if (e.getMessage().contains("Conflict")) {
          return new HttpResponse(HttpStatus.CONFLICT, ContentType.JSON,
              createJsonMessage("error", "Deck is already configured"));
        }
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Internal Server Error at Database Level"));
      }
    }
  }

  public HttpResponse deleteDeal(final HttpRequest request) {
    try {
      final var tradeId = UUID.fromString(request.getPathSegments().get(2));
      final var trade = tradingDbAccess.getTradeFromId(tradeId);
      if (trade == null) {
        return new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON,
            createJsonMessage("error", "The provided deal ID was not found"));
      }
      final var user = request.getUser();
      if (user == null) {
        return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
            createJsonMessage("error", "Access token is missing or invalid"));
      } else if (user.getId().equals(tradingDbAccess.getTradeFromId(tradeId))) {
        return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON,
            createJsonMessage("error", "The deal contains a card that is not owned by the user."));
      } else {
        tradingDbAccess.deleteDeal(tradeId);
        return new HttpResponse(HttpStatus.OK, ContentType.JSON, "\n");
      }
    } catch (SQLException e) {
      return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
          createJsonMessage("error", "Internal Server Error at Database Level"));
    }
  }

  public HttpResponse trade(final HttpRequest request) {
    try {
      // Get the trade ID from the request path
      final var tradeId = UUID.fromString(request.getPathSegments().get(2));

      // Fetch the trade details from the database
      final var trade = tradingDbAccess.getTradeFromId(tradeId);
      if (trade == null) {
        return new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON,
            createJsonMessage("error", "The provided deal ID was not found"));
      }

      // Get the user making the trade
      final var user = request.getUser();
      if (user == null) {
        return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
            createJsonMessage("error", "Access token is missing or invalid"));
      }

      // Check if the user is trying to trade with themselves
      if (trade.getUserId().equals(user.getId())) {
        return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON,
            createJsonMessage("error", "You cannot trade with yourself"));
      }

      // Get the card ID from the request body
      final var cardId = UUID.fromString(request.getBody().replaceAll("\"", ""));

      // Fetch card details from the database
      final var card = cardDbAccess.getCardById(cardId);
      if (card == null) {
        return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON,
            createJsonMessage("error", "The provided card ID was not found"));
      }

      // Validate the card type and damage
      if (!card.getCardType().toString().equalsIgnoreCase(trade.getRequiredType())
          || card.getDamage() < trade.getMinDamage()) {
        return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON,
            createJsonMessage("error", "The card does not meet the trade requirements"));
      }

      // Perform the trade
      if (tradingDbAccess.completeTrade(tradeId, user.getId(), cardId)) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON,
            createJsonMessage("success", "Trade completed successfully"));
      } else {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Failed to complete the trade due to a database error"));
      }

    } catch (IllegalArgumentException e) {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid trade or card ID format"));
    } catch (SQLException e) {
      return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
          createJsonMessage("error", "Internal Server Error at Database Level"));
    }
  }

}
