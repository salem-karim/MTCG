package org.mtcg.utils;

import org.mtcg.services.UserService;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RouterTest {

  @Test
  public void testAddService() {
    final Router router = new Router();
    final UserService userService = new UserService();
    router.addService("/users", userService);

    assertNotNull(router.resolve("/users"));
  }

  @Test
  public void testRemoveService() {
    final Router router = new Router();
    final UserService userService = new UserService();
    router.addService("/users", userService);
    router.removeService("/users");

    assertNull(router.resolve("/users"));
  }
}
