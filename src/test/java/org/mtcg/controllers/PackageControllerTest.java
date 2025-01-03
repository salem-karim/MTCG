package org.mtcg.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.mtcg.db.PackageDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Card;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

import java.util.UUID;

class PackageControllerTest {
  private PackageController packageController;

  @Mock
  private PackageDbAccess mockPackageDbAccess;

  @Mock
  private User mockUser;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Create the PackageController with a mock PackageDbAccess
    packageController = new PackageController(mockPackageDbAccess);
  }

  @Test
  void testAddPackage_Successful() throws Exception {
    // Prepare test data
    final UUID userId = UUID.randomUUID();
    when(mockUser.getId()).thenReturn(userId);
    when(mockUser.getUsername()).thenReturn("admin");

    // Prepare JSON representation of cards
    final String cardsJson = """
        [
            {"Id": "%s", "Name": "Ork", "Damage": 50.0},
            {"Id": "%s", "Name": "Water Spell", "Damage": 30.5},
            {"Id": "%s", "Name": "Knight", "Damage": 40.0},
            {"Id": "%s", "Name": "Fire Dragon", "Damage": 45.0},
            {"Id": "%s", "Name": "KnifeSpell", "Damage": 35.0}
        ]
        """.formatted(
        UUID.randomUUID(), UUID.randomUUID(),
        UUID.randomUUID(), UUID.randomUUID(),
        UUID.randomUUID());

    // Mock request setup
    HttpRequest mockRequest = mock(HttpRequest.class);
    when(mockRequest.getUser()).thenReturn(mockUser);
    when(mockRequest.getBody()).thenReturn(cardsJson);

    // Mock successful package addition
    when(mockPackageDbAccess.addPackage(any())).thenReturn(true);

    // Verify card parsing
    final Card[] parsedCards = packageController.getObjectMapper().readValue(cardsJson, Card[].class);
    assertEquals(5, parsedCards.length, "Should parse 5 cards correctly");

    // Call method under test
    final HttpResponse response = packageController.addPackage(mockRequest);
    final String shouldEqual = packageController.createJsonMessage("message", "Package created successfully");

    // Assertions
    assertEquals(HttpStatus.CREATED.code, response.getStatusCode(), "Status should be CREATED");
    assertEquals(ContentType.JSON.toString(), response.getContentType());
    assertEquals(shouldEqual, response.getBody());

    // Verify that addPackage was called
    verify(mockPackageDbAccess).addPackage(any());
  }

  @Test
  void testAddPackage_InsufficientCards() {
    // Prepare test data with fewer than 5 cards
    final UUID userId = UUID.randomUUID();
    when(mockUser.getId()).thenReturn(userId);
    when(mockUser.getUsername()).thenReturn("admin");

    // Prepare JSON representation of cards
    final String cardsJson = """
        [
            {"Id": "%s", "Name": "Ork", "Damage": 50.0},
            {"Id": "%s", "Name": "Water Spell", "Damage": 30.5},
            {"Id": "%s", "Name": "Knight", "Damage": 40.0}
        ]
        """.formatted(
        UUID.randomUUID(), UUID.randomUUID(),
        UUID.randomUUID());

    // Mock request setup
    HttpRequest mockRequest = mock(HttpRequest.class);
    when(mockRequest.getUser()).thenReturn(mockUser);
    when(mockRequest.getBody()).thenReturn(cardsJson);

    // Call method under test
    final HttpResponse response = packageController.addPackage(mockRequest);
    final String shouldEqual = packageController.createJsonMessage("error",
        "The provided package did not include the required amount of cards");

    // Assertions
    assertEquals(HttpStatus.BAD_REQUEST.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType());
    assertEquals(shouldEqual, response.getBody());
  }

  @Test
  void testAddPackage_Unauthorized() {
    // Simulate no user (unauthorized request)
    HttpRequest mockRequest = mock(HttpRequest.class);
    when(mockRequest.getUser()).thenReturn(null);

    // Call method under test
    final HttpResponse response = packageController.addPackage(mockRequest);
    final String shouldEqual = packageController.createJsonMessage("error",
        "Access token is missing or invalid");

    // Assertions
    assertEquals(HttpStatus.UNAUTHORIZED.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType());
    assertEquals(shouldEqual, response.getBody());
  }

  @Test
  void testAddPackage_DatabaseFailure() throws Exception {
    // Prepare test data
    final UUID userId = UUID.randomUUID();
    when(mockUser.getId()).thenReturn(userId);
    when(mockUser.getUsername()).thenReturn("admin");

    // Prepare JSON representation of cards
    final String cardsJson = """
        [
            {"Id": "%s", "Name": "Ork", "Damage": 50.0},
            {"Id": "%s", "Name": "Water Spell", "Damage": 30.5},
            {"Id": "%s", "Name": "Knight", "Damage": 40.0},
            {"Id": "%s", "Name": "Fire Dragon", "Damage": 45.0},
            {"Id": "%s", "Name": "KnifeSpell", "Damage": 35.0}
        ]
        """.formatted(
        UUID.randomUUID(), UUID.randomUUID(),
        UUID.randomUUID(), UUID.randomUUID(),
        UUID.randomUUID());

    // Mock request setup
    HttpRequest mockRequest = mock(HttpRequest.class);
    when(mockRequest.getUser()).thenReturn(mockUser);
    when(mockRequest.getBody()).thenReturn(cardsJson);

    // Mock database failure
    when(mockPackageDbAccess.addPackage(any())).thenReturn(false);

    // Call method under test
    final HttpResponse response = packageController.addPackage(mockRequest);
    final String shouldEqual = packageController.createJsonMessage("error", "Failed to add Package");

    // Assertions
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType());
    assertEquals(shouldEqual, response.getBody());
  }
}
