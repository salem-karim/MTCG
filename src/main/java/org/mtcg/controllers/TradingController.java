package org.mtcg.controllers;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.mtcg.db.CardDbAccess;
import org.mtcg.db.TradingDbAccess;
import org.mtcg.db.DbConnection;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Trade;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TradingController extends Controller {
  private final TradingDbAccess tradingDbAccess;
  private final CardDbAccess cardDbAccess;

  public TradingController(final TradingDbAccess tradeDb, final CardDbAccess cardDb) {
    super();
    this.tradingDbAccess = tradeDb;
    this.cardDbAccess = cardDb;
  }

  public TradingController() {
    this(new TradingDbAccess(), new CardDbAccess());
  }

  public HttpResponse listDeals(final HttpRequest request) {
    if (request.getUser() == null) {
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    } else {
      // Get all Trading Deals and respond with formatted JSON
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
      return new HttpResponse(
          HttpStatus.UNAUTHORIZED,
          ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    }

    try {
      // Deserialize JSON body into a Trade object
      final Trade partialTrade = getObjectMapper().readValue(request.getBody(), Trade.class);

      // Add userId to the Trade object
      final Trade trade = partialTrade.withUserId(request.getUser().getId());

      // Insert the trading deal into the database
      tradingDbAccess.createDeal(trade, request.getUser().getId());

      return new HttpResponse(HttpStatus.CREATED, ContentType.JSON, "\n");

    } catch (JsonProcessingException e) {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid request body. Check the JSON format."));

    } catch (SQLException e) {
      if (e.getMessage().contains("Conflict")) {
        // Check if Deal of that ID already exists in the Database
        return new HttpResponse(HttpStatus.CONFLICT, ContentType.JSON,
            createJsonMessage("error", "A deal with this ID already exists."));
      } else if (e.getMessage().contains("The Card is not in the user's stack.")) {
        // Check if Card of the Deal is actually in users Stack
        return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON,
            createJsonMessage("error", "The card does not belong to the user or is unavailable."));
      } else {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Internal server error. Please try again later."));
      }
    }
  }

  public HttpResponse deleteDeal(final HttpRequest request) {
    try {
      final var tradeId = UUID.fromString(request.getPathSegments().get(1));

      // Retrieve the trade from the database
      final var trade = tradingDbAccess.getTradeFromId(tradeId);
      if (trade == null) {
        return new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON,
            createJsonMessage("error", "The provided deal ID was not found"));
      }

      // Validate the user's identity
      final var user = request.getUser();
      if (user == null) {
        return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
            createJsonMessage("error", "Access token is missing or invalid"));
      }

      // Check if the user owns the trade
      if (!user.getId().equals(trade.getUserId())) {
        return new HttpResponse(HttpStatus.FORBIDDEN, ContentType.JSON,
            createJsonMessage("error", "The deal is not owned by this user"));
      }

      // Perform the deletion
      tradingDbAccess.deleteDeal(DbConnection.getConnection(), tradeId);

      // Return a successful response
      return new HttpResponse(HttpStatus.OK, ContentType.JSON,
          createJsonMessage("message", "Trading deal successfully deleted"));
    } catch (IllegalArgumentException e) {
      // Handle invalid UUID format
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid trade ID format."));
    } // Log and return database-level error
    catch (Exception e) {
      // Catch any other unexpected exceptions
      System.err.println("Unexpected Exception: " + e.getMessage());
      return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
          createJsonMessage("error", "An unexpected error occurred."));
    }
  }

  public HttpResponse trade(final HttpRequest request) {
    try {
      // Get the trade ID from the request path
      final var tradeId = UUID.fromString(request.getPathSegments().get(1));

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
      final var cardId = getObjectMapper().readValue(request.getBody(), UUID.class);

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
      if (tradingDbAccess.completeTrade(trade, user.getId(), cardId)) {
        return new HttpResponse(HttpStatus.CREATED, ContentType.JSON,
            createJsonMessage("success", "Trade completed successfully"));
      } else {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Failed to complete the trade due to a database error"));
      }

    } catch (IllegalArgumentException e) {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid trade or card ID format"));
    } catch (JsonProcessingException e) {
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Invalid request body. Check the JSON format."));

    } catch (SQLException e) {
      return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
          createJsonMessage("error", "Internal Server Error at Database Level"));
    }
  }

}
