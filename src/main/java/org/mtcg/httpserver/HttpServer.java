package org.mtcg.httpserver;

import org.mtcg.utils.Router;

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
      while (running) {
        Socket client = socket.accept();
        logger.info("Accepted connection from " + client.getRemoteSocketAddress());

        pool.execute(() -> {
          try (var input = client.getInputStream();
               var output = client.getOutputStream();
               var inputReader = new InputStreamReader(input);
               var bufferReader = new BufferedReader(inputReader);
               var printWriter = new PrintWriter(output)) {

            HttpRequest req = new HttpRequest(bufferReader);
            HttpRequestHandler handler = new HttpRequestHandler(this.router);
            handler.handleRequest(printWriter, req);
          } catch (IOException e) {
            logger.log(Level.SEVERE, "Error handling request", e);
          } finally {
            try {
              client.close();
            } catch (IOException e) {
              logger.log(Level.SEVERE, "Error closing client socket", e);
            }
          }
        });
      }
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error starting server", e);
    } finally {
      close();
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
      logger.log(Level.SEVERE, "Error closing server", e);
    }
  }
}
