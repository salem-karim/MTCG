package org.mtcg.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtcg.controllers.UserController;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.Method;

public class UserServiceTest {
  private UserService userService;
  private UserController mockUserController;

  @BeforeEach
  public void setUp() {
    mockUserController = mock(UserController.class);
    userService = new UserService();
    // Overwrite Initial UserController with mocked one
    userService.methods.put(Method.POST, mockUserController::addUser);
  }

  @Test
  void testHandlePostRequest() {
    // Arrange: Create a mock request and response
    final HttpRequest mockRequest = new HttpRequest(Method.POST, "/user", "some request body", "");
    final HttpResponse mockResponse = new HttpResponse(HttpStatus.CREATED, ContentType.JSON, "User created");
    when(mockUserController.addUser(mockRequest)).thenReturn(mockResponse);

    // Act: Call handle and get the response
    final HttpResponse response = userService.handle(mockRequest);

    // Assert: Verify the response with StatusCode and response body
    assertEquals(HttpStatus.CREATED.code, response.getStatusCode());
    assertEquals("User created", response.getBody());
    // Lastly verify that mocked addUser worked
    verify(mockUserController).addUser(mockRequest);
  }

  @Test
  void testHandleUnsupportedMethod() {
    // Arrange: Create a mock request with an unsupported method
    final HttpRequest mockRequest = new HttpRequest(Method.DELETE, "/user", null, "");

    // Act: Call handle and get the response
    final HttpResponse response = userService.handle(mockRequest);

    // Assert: Verify that defaultResponse is used
    assertEquals(400, response.getStatusCode());
    assertEquals("Bad Request", response.getBody());
  }
}
