package org.mtcg.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mtcg.db.TransactionDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.User;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TransactionControllerTest {

  private TransactionController transactionController;

  @Mock
  private TransactionDbAccess mockTransactionDbAccess;

  @Mock
  private HttpRequest mockRequest;

  @Mock
  private User mockUser;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    transactionController = new TransactionController(mockTransactionDbAccess);
  }

  @Test
  void testBuyPackage_SuccessfulTransaction() throws SQLException {
    // Mocking user with sufficient coins
    when(mockRequest.getUser()).thenReturn(mockUser);
    when(mockUser.getCoins()).thenReturn(10);

    // Mocking database operations
    final UUID packageId = UUID.randomUUID();
    final UUID[] cardIds = { UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID() };
    final UUID stackId = UUID.randomUUID();

    when(mockTransactionDbAccess.getRandomPackage()).thenReturn(packageId);
    when(mockTransactionDbAccess.getPackageCards(packageId)).thenReturn(cardIds);
    when(mockTransactionDbAccess.getStackId(mockUser.getId())).thenReturn(stackId);

    // Call the method under test
    HttpResponse response = transactionController.buyPackage(mockRequest);

    // Assertions
    assertEquals(HttpStatus.CREATED.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType());
    assertEquals(transactionController.createJsonMessage("message", "Package purchased successfully"),
        response.getBody());

    // Verifications
    verify(mockTransactionDbAccess).performTransaction(mockUser, packageId, cardIds, stackId);
    verify(mockUser).setCoins(5); // Coins reduced from 10 to 5
  }

  @Test
  void testBuyPackage_NotEnoughMoney() {
    // Mocking user with insufficient coins
    when(mockRequest.getUser()).thenReturn(mockUser);
    when(mockUser.getCoins()).thenReturn(2);

    // Call the method under test
    HttpResponse response = transactionController.buyPackage(mockRequest);

    // Assertions
    assertEquals(HttpStatus.FORBIDDEN.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType());
    assertEquals(transactionController.createJsonMessage("error", "Not enough money for buying a card package"),
        response.getBody());

    // No database operations should be performed
    verifyNoInteractions(mockTransactionDbAccess);
  }

  @Test
  void testBuyPackage_NoAvailablePackages() throws SQLException {
    // Mocking user with sufficient coins
    when(mockRequest.getUser()).thenReturn(mockUser);
    when(mockUser.getCoins()).thenReturn(10);

    // Mocking no available packages in the database
    when(mockTransactionDbAccess.getRandomPackage()).thenReturn(null);

    // Call the method under test
    HttpResponse response = transactionController.buyPackage(mockRequest);

    // Assertions
    assertEquals(HttpStatus.NOT_FOUND.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType());
    assertEquals(transactionController.createJsonMessage("error", "No card packages available for buying"),
        response.getBody());
  }

  @Test
  void testBuyPackage_NoValidStackForUser() throws SQLException {
    // Mocking user with sufficient coins
    when(mockRequest.getUser()).thenReturn(mockUser);
    when(mockUser.getCoins()).thenReturn(10);

    // Mocking valid package but no valid stack ID
    final UUID packageId = UUID.randomUUID();
    final UUID[] cardIds = { UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID() };
    when(mockTransactionDbAccess.getRandomPackage()).thenReturn(packageId);
    when(mockTransactionDbAccess.getPackageCards(packageId)).thenReturn(cardIds);
    when(mockTransactionDbAccess.getStackId(mockUser.getId())).thenReturn(null);

    // Call the method under test
    HttpResponse response = transactionController.buyPackage(mockRequest);

    // Assertions
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType());
    assertEquals(transactionController.createJsonMessage("error", "User does not have a valid stack"),
        response.getBody());
  }

  @Test
  void testBuyPackage_DatabaseError() throws SQLException {
    // Mocking user with sufficient coins
    when(mockRequest.getUser()).thenReturn(mockUser);
    when(mockUser.getCoins()).thenReturn(10);

    // Mocking database error during transaction
    final UUID packageId = UUID.randomUUID();
    final UUID[] cardIds = { UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID() };
    final UUID stackId = UUID.randomUUID();

    when(mockTransactionDbAccess.getRandomPackage()).thenReturn(packageId);
    when(mockTransactionDbAccess.getPackageCards(packageId)).thenReturn(cardIds);
    when(mockTransactionDbAccess.getStackId(mockUser.getId())).thenReturn(stackId);
    doThrow(new RuntimeException("Database error")).when(mockTransactionDbAccess).performTransaction(any(), any(),
        any(), any());

    // Call the method under test
    HttpResponse response = transactionController.buyPackage(mockRequest);

    // Assertions
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType());
    assertEquals(transactionController.createJsonMessage("error", "An error occurred during package purchase"),
        response.getBody());
  }

  @Test
  void testBuyPackage_UnauthorizedUser() {
    // Simulate no user in the request
    when(mockRequest.getUser()).thenReturn(null);

    // Call the method under test
    HttpResponse response = transactionController.buyPackage(mockRequest);

    // Assertions
    assertEquals(HttpStatus.UNAUTHORIZED.code, response.getStatusCode());
    assertEquals(ContentType.JSON.toString(), response.getContentType());
    assertEquals(transactionController.createJsonMessage("error", "Access token is missing or invalid"),
        response.getBody());
  }
}
