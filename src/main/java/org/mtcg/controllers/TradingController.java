package org.mtcg.controllers;

import java.util.List;

import org.mtcg.db.TradingDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Trade;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TradingController extends Controller {
  private final TradingDbAccess tradingDbAccess;

  public TradingController() {
    super();
    this.tradingDbAccess = new TradingDbAccess();
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
      return null;
    }
  }

  public HttpResponse deleteDeal(final HttpRequest request) {
    if (request.getUser() == null) {
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    } else {
      return null;
    }
  }

  public HttpResponse trade(final HttpRequest request) {
    if (request.getUser() == null) {
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    } else {
      return null;
    }
  }

}
