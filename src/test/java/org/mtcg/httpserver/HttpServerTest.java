package org.mtcg.httpserver;

import org.mtcg.utils.Router;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;


public class HttpServerTest {
  private HttpServer server;
  private Thread serverThread;

  @BeforeEach
  public void setUp() {
    Router router = new Router();
    server = new HttpServer(8080, router);
    serverThread = new Thread(server);
    serverThread.start();
  }

  @Test
  public void testServerClose() throws IOException, InterruptedException {
    // Wait a bit to ensure the server is up
    Thread.sleep(1000);

    // Check if the server socket is open
    ServerSocket socket = new ServerSocket(8080);
    assertFalse(socket.isClosed());
    socket.close();

    // Close the server
    server.close();

    // Wait a bit to ensure the server has closed
    Thread.sleep(1000);

    // Check if the server socket is closed
    assertTrue(serverThread.isAlive());
  }

  @AfterEach
  public void tearDown() throws InterruptedException {
    if (serverThread.isAlive()) {
      server.close();
      serverThread.join();
    }
  }
}
