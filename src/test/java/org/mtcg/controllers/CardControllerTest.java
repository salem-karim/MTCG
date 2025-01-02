package org.mtcg.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mtcg.db.CardDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Card;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardControllerTest {

  @Mock
  private CardDbAccess cardDbAccess;

  @Mock
  private HttpRequest request;

  private CardController cardController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    cardController = new CardController(cardDbAccess);
  }

  @Test
  void listCards_WithValidUserAndCards_ReturnsOkResponse() {
    // Arrange
    User mockUser = new User("testUser", "password");
    when(request.getUser()).thenReturn(mockUser);

    ArrayList<Card> mockCards = new ArrayList<>();

    mockCards.add(new Card(UUID.randomUUID(), "WaterSpell", 10.0, Card.CardType.SPELL, Card.Element.WATER));
    mockCards.add(new Card(UUID.randomUUID(), "FireGoblin", 15.0, Card.CardType.MONSTER, Card.Element.FIRE));

    when(cardDbAccess.getCards(mockUser.getId())).thenReturn(mockCards);

    // Act
    HttpResponse response = cardController.listCards(request);

    // Assert
    assertEquals(HttpStatus.OK.code, response.getStatusCode());
    assertEquals(ContentType.JSON.name(), response.getContentType());
    assertTrue(response.getBody().contains("WaterSpell"));
    assertTrue(response.getBody().contains("FireGoblin"));
    verify(cardDbAccess).getCards(mockUser.getId());
  }

  @Test
  void listCards_WithValidUserNoCards_ReturnsNoContentResponse() {
    // Arrange
    User mockUser = new User("testUser", "password");
    when(request.getUser()).thenReturn(mockUser);

    when(cardDbAccess.getCards(mockUser.getId())).thenReturn(new ArrayList<>());

    // Act
    HttpResponse response = cardController.listCards(request);

    // Assert
    assertEquals(HttpStatus.NO_CONTENT.code, response.getStatusCode());
    assertEquals(ContentType.JSON.name(), response.getContentType());
    assertEquals("\n", response.getBody());
    verify(cardDbAccess).getCards(mockUser.getId());
  }

  @Test
  void listCards_WithNoUser_ReturnsUnauthorizedResponse() {
    // Arrange
    when(request.getUser()).thenReturn(null);

    // Act
    HttpResponse response = cardController.listCards(request);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED.code, response.getStatusCode());
    assertEquals(ContentType.JSON.name(), response.getContentType());
    assertTrue(response.getBody().contains("Access token is missing or invalid"));
    verify(cardDbAccess, never()).getCards(any(UUID.class));
  }

  @Test
  void listCards_WithDatabaseError_ReturnsBadRequestResponse() {
    // Arrange
    User mockUser = new User("testUser", "password");
    when(request.getUser()).thenReturn(mockUser);

    when(cardDbAccess.getCards(mockUser.getId())).thenReturn(null);

    // Act
    HttpResponse response = cardController.listCards(request);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST.code, response.getStatusCode());
    assertEquals(ContentType.JSON.name(), response.getContentType());
    assertTrue(response.getBody().contains("Bad Request"));
    verify(cardDbAccess).getCards(mockUser.getId());
  }
}
