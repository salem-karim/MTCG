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

public class HttpServer implements Runnable {
  private static ServerSocket socket = null;
  private final int port;
  private final Router router;
  private final ExecutorService pool = Executors.newFixedThreadPool(8);

  public HttpServer(int port, Router router) {
    this.port = port;
    this.router = router;
  }

  @Override
  public void run() {
    try {
      socket = new ServerSocket(port);
      System.out.println("HTTP Server started on port " + port);
      while (true) {
        // Accept a client connection
        Socket client = socket.accept();
        System.out.println("HTTP Server accepted connection from " + client.getRemoteSocketAddress());

        // Handle the request in a separate thread
        pool.execute(() -> {
          try {
            // Create the input and output streams with try-with-resources
            try (var input = client.getInputStream();
                 var output = client.getOutputStream();
                 var inputReader = new InputStreamReader(input);
                 var bufferReader = new BufferedReader(inputReader);
                 var printWriter = new PrintWriter(output)) {

              // Create the request object from the client's input stream
              HttpRequest req = new HttpRequest(bufferReader);
              HttpRequestHandler handler = new HttpRequestHandler(this.router);
              handler.handleRequest(printWriter, req); // Handle the request
            } // The PrintWriter, BufferedReader, and InputStreamReader are automatically closed here

          } catch (IOException e) {
            System.err.println("Error handling request: " + e.getMessage());
          } finally {
            // Ensure the client socket is closed after handling the request
            try {
                client.close(); // Close the client socket
            } catch (IOException e) {
              System.err.println("Error closing client socket: " + e.getMessage());
            }
          }
        });

      }
    } catch (IOException e) {
      System.err.println("Error starting server: " + e.getMessage());
    } finally {
      close();
    }
  }

  public void close() {
    System.out.println("HTTP Server closing on port " + port);
    try {
      pool.shutdown();
      if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
        pool.shutdownNow();
      }
      if (socket != null && !socket.isClosed()) {
        socket.close();
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
