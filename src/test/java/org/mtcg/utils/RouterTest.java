package org.mtcg.utils;

import org.junit.Test;
import org.mtcg.service.UserService;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

public class RouterTest {

  @Test
  public void testAddService() {
    Router router = new Router();
    UserService userService = new UserService();
    router.addService("/users", userService);

    assertNotNull(router.resolve("/users"));
  }

  @Test
  public void testRemoveService() {
    Router router = new Router();
    UserService userService = new UserService();
    router.addService("/users", userService);
    router.removeService("/users");

    assertNull(router.resolve("/users"));
  }
}
