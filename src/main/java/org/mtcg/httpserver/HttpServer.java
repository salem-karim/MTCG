package org.mtcg.httpserver;

import org.mtcg.utils.battle.BattleLobby;
import org.mtcg.utils.Router;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer implements Runnable {
  private static final Logger logger = Logger.getLogger(HttpServer.class.getName());
  private final int port;
  private final Router router;
  private ServerSocket serverSocket;
  private final ExecutorService threadPool = Executors.newFixedThreadPool(12);
  private volatile boolean running = true;

  public HttpServer(final int port, final Router router) {
    this.port = port;
    this.router = router;
  }

  @Override
  public void run() {
    try {
      serverSocket = new ServerSocket(port);
      logger.info("HTTP Server started on port " + port);

      while (running) {
        try {
          final Socket clientSocket = serverSocket.accept();
          logger.info("Accepted connection from " + clientSocket.getRemoteSocketAddress());
          threadPool.submit(new HttpRequestHandler(clientSocket, router));
        } catch (final IOException e) {
          if (running) {
            logger.log(Level.SEVERE, "Error accepting connection", e);
          } else {
            logger.info("Client socket closed, stopping server.");
          }
        }
      }
    } catch (final IOException e) {
      logger.log(Level.SEVERE, "Could not start server", e);
    } finally {
      close();
    }
  }

  public void close() {
    if (!running)
      return;
    running = false;
    if (serverSocket != null && !serverSocket.isClosed()) {
      try {
        serverSocket.close();
      } catch (final IOException e) {
        logger.log(Level.SEVERE, "Error closing server socket", e);
      }
    }
    BattleLobby.getInstance().shutdown();
    threadPool.shutdown();
    logger.info("HTTP Server closing on port " + port);
  }
}
