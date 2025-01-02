package org.mtcg.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mtcg.db.CardDbAccess;
import org.mtcg.db.TradingDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Trade;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

public class TradingControllerTest {
  private TradingController tradingController;

  @Mock
  private TradingDbAccess mockTradingDbAccess;

  @Mock
  private CardDbAccess mockCardDbAccess;

  @Mock
  private HttpRequest mockRequest;

  @Mock
  private User mockUser;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    tradingController = new TradingController(mockTradingDbAccess, mockCardDbAccess);
  }

  @Test
  void listDeals_unauthorizedUser_returnsUnauthorizedResponse() {
    when(mockRequest.getUser()).thenReturn(null);

    HttpResponse response = tradingController.listDeals(mockRequest);

    assertEquals(HttpStatus.UNAUTHORIZED.code, response.getStatusCode());
    assertEquals(ContentType.JSON.name(), response.getContentType());
    assertTrue(response.getBody().contains("Access token is missing or invalid"));
  }

  @Test
  void createDeal_invalidJsonBody_returnsBadRequest() throws Exception {
    when(mockRequest.getUser()).thenReturn(mockUser);
    when(mockRequest.getBody()).thenReturn("invalid-json");

    HttpResponse response = tradingController.createDeal(mockRequest);

    assertEquals(HttpStatus.BAD_REQUEST.code, response.getStatusCode());
    assertTrue(response.getBody().contains("Invalid request body"));
  }

  @Test
  void createDeal_dealWithIdAlreadyExists_returnsConflict() throws Exception {
    // prepare used data in controller method
    final String tradeJson = """
        {\"Id\": \"6cd85277-4590-49d4-b0cf-ba0a921faad0\",
        \"CardToTrade\": \"1cb6ab86-bdb2-47e5-b6e4-68c5ab389334\",
        \"Type\": \"monster\", \"MinimumDamage\": 15}""";

    // Mock request behavior
    when(mockUser.getId()).thenReturn(UUID.randomUUID());
    when(mockRequest.getUser()).thenReturn(mockUser);
    when(mockRequest.getBody()).thenReturn(tradeJson);

    // Mock db conflict to test catching of Exceptions
    doThrow(new SQLException("Conflict"))
        .when(mockTradingDbAccess)
        .createDeal(any(Trade.class), any(UUID.class));

    HttpResponse response = tradingController.createDeal(mockRequest);

    assertEquals(HttpStatus.CONFLICT.code, response.getStatusCode());
    assertEquals(tradingController.createJsonMessage("error", "A deal with this ID already exists."),
        response.getBody());
  }

  @Test
  void deleteDeal_tradeNotFound_returnsNotFound() throws Exception {
    UUID tradeId = UUID.randomUUID();
    when(mockRequest.getPathSegments()).thenReturn(List.of("trade", tradeId.toString()));
    when(mockTradingDbAccess.getTradeFromId(tradeId)).thenReturn(null);

    HttpResponse response = tradingController.deleteDeal(mockRequest);

    assertEquals(HttpStatus.NOT_FOUND.code, response.getStatusCode());
    assertTrue(response.getBody().contains("The provided deal ID was not found"));
  }

}
