package org.mtcg.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mtcg.db.PackageDbAccess;
import org.mtcg.db.UserDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.Package;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.Method;
import org.mtcg.utils.exceptions.HttpRequestException;

//TODO: Fix Object Mapper Issue with mocking
@ExtendWith(MockitoExtension.class)
public class PackageControllerTest {

  @Mock
  private PackageDbAccess pkgDbAccess;

  @Mock
  private UserDbAccess userDbAccess;

  private PackageController packageController;

  private UUID userId;
  private Map<String, String> headers;
  private User user;

  @BeforeEach
  public void setUp() {
    userId = UUID.randomUUID();
    headers = new HashMap<>();
    headers.put("Authorization", "Bearer testUser-mtcgToken");

    // Initialize a User with all required parameters
    user = new User("testUser", "testUser-mtcgToken", "securePassword", userId);
    user.setId(userId);

    // Explicitly create the controller with mocks
    packageController = new PackageController(pkgDbAccess, userDbAccess);
  }

  @Test
  void testAddPackage_SuccessfulResponse() throws Exception {
    // Arrange
    String requestBody = """
        [
            {"id": "%s", "name": "Ork", "damage": 50.0},
            {"id": "%s", "name": "Water Spell", "damage": 30.5},
            {"id": "%s", "name": "Knight", "damage": 40.0},
            {"id": "%s", "name": "Fire Dragon", "damage": 45.0},
            {"id": "%s", "name": "KnifeSpell", "damage": 35.0}
        ]
        """.formatted(
        UUID.randomUUID(), UUID.randomUUID(),
        UUID.randomUUID(), UUID.randomUUID(),
        UUID.randomUUID());

    HttpRequest request = new HttpRequest(
        Method.POST,
        "/packages",
        requestBody,
        headers);

    // Mock behavior with eq() for matching the headers
    when(userDbAccess.getUserFromToken(eq(headers)))
        .thenReturn(user);
    when(pkgDbAccess.addPackage(any(Package.class))).thenReturn(true);

    // Act
    HttpResponse response = packageController.addPackage(request);

    // Assert
    assertEquals(HttpStatus.CREATED.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType().toString());
    assertEquals("Package created successfully\n", response.getBody());

    verify(userDbAccess).getUserFromToken(eq(headers)); // Use eq() matcher for headers
    verify(pkgDbAccess).addPackage(any(Package.class));
  }

  @Test
  void testAddPackage_UnauthorizedResponse() throws Exception {
    // Arrange
    HttpRequest request = new HttpRequest(
        Method.POST,
        "/packages",
        "[{\"id\": \"" + UUID.randomUUID() + "\", \"name\":\"Ork\",\"damage\":50.0}]",
        headers);

    when(userDbAccess.getUserFromToken(eq(headers))) // Use eq() matcher for headers
        .thenThrow(new HttpRequestException("Authorization header is missing or invalid"));

    // Act
    HttpResponse response = packageController.addPackage(request);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType().toString());
    assertEquals("Authorization header is missing or invalid\n", response.getBody());

    verify(userDbAccess).getUserFromToken(eq(headers)); // Use eq() matcher for headers
    verify(pkgDbAccess, never()).addPackage(any(Package.class));
  }

  @Test
  void testAddPackage_BadRequestResponse() throws Exception {
    // Arrange: Body with less than 5 cards
    String requestBody = """
        [
            {"id": "%s", "name": "Ork", "damage": 50.0},
            {"id": "%s", "name": "Water Spell", "damage": 30.5}
        ]
        """.formatted(UUID.randomUUID(), UUID.randomUUID());

    HttpRequest request = new HttpRequest(
        Method.POST,
        "/packages",
        requestBody,
        headers);

    when(userDbAccess.getUserFromToken(eq(headers))) // Use eq() matcher for headers
        .thenReturn(user);

    // Act
    HttpResponse response = packageController.addPackage(request);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType().toString());
    assertEquals("Not 5 cards in Request Body\n", response.getBody()); // Update to match the controller's message

    verify(userDbAccess).getUserFromToken(eq(headers)); // Use eq() matcher for headers
    verify(pkgDbAccess, never()).addPackage(any(Package.class));
  }
}
