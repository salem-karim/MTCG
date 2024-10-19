package org.mtcg.httpserver;

import org.mtcg.utils.exceptions.ClientHandlingException;
import org.mtcg.utils.Router;
import org.mtcg.utils.exceptions.HttpRequestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer implements Runnable {
  private static final Logger logger = Logger.getLogger(HttpServer.class.getName());
  private static ServerSocket socket = null;
  private final int port;
  private final Router router;
  private final ExecutorService pool = Executors.newFixedThreadPool(8);
  private volatile boolean running = true;

  public HttpServer(int port, Router router) {
    this.port = port;
    this.router = router;
  }

  @Override
  public void run() {
    try {
      socket = new ServerSocket(port);
      logger.info("HTTP Server started on port " + port);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error starting server on port " + port, e);
      return;
    }
    try {
      while (running) {
        Socket client = socket.accept();
        logger.info("Accepted connection from " + client.getRemoteSocketAddress());

        pool.execute(() -> {
          try {
            handleClient(client);
          } catch (ClientHandlingException ex) {
            logger.log(Level.SEVERE, "Error handling client", ex);
          }
        });
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error accepting connection", e);
    } finally {
      close();
    }
  }

  private void handleClient(Socket client) throws ClientHandlingException {
    try (var input = client.getInputStream();
         var output = client.getOutputStream();
         var inputReader = new InputStreamReader(input);
         var bufferReader = new BufferedReader(inputReader);
         var printWriter = new PrintWriter(output)) {

      HttpRequest req = new HttpRequest(bufferReader);
      HttpRequestHandler handler = new HttpRequestHandler(this.router);
      handler.handleRequest(printWriter, req);
    } catch (HttpRequestException e) {
      throw new ClientHandlingException("Error parsing HTTP request from client " + client.getRemoteSocketAddress(), e);
    } catch (IOException e) {
      throw new ClientHandlingException("Error handling request from client " + client.getRemoteSocketAddress(), e);
    } finally {
      try {
        client.close();
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error closing client socket", e);
      }
    }
  }

  public void close() {
    logger.info("HTTP Server closing on port " + port);
    running = false;
    try {
      pool.shutdown();
      if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
        pool.shutdownNow();
      }
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException | InterruptedException e) {
      logger.log(Level.SEVERE, "Error closing server on port " + port, e);
    }
  }
}
