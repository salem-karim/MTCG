package org.mtcg.httpserver;

import org.junit.jupiter.api.*;
import org.mtcg.utils.Router;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

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
  public void testServerStartAndStop() throws InterruptedException {
    // Wait a bit to ensure the server is up
    Thread.sleep(1000);

    // Check if the server thread is alive
    assertTrue(serverThread.isAlive());

    // Close the server
    server.close();
    serverThread.join();

    // Check if the server thread has stopped
    assertFalse(serverThread.isAlive());
  }

  @Test
  public void testRequestHandling() throws IOException, InterruptedException {
    // Wait a bit to ensure the server is up
    Thread.sleep(1000);

    // Send a test request to the server
    URI uri = URI.create("http://localhost:8080/");
    HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
    connection.setRequestMethod("GET");

    int responseCode = connection.getResponseCode();
    assertEquals(200, responseCode);

    // Read the response
    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String inputLine;
    StringBuilder content = new StringBuilder();
    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }
    in.close();

    // Verify the response content
    assertTrue(content.toString().contains("Welcome to the homepage!"));

    // Close the server
    server.close();
    serverThread.join();
  }
}
