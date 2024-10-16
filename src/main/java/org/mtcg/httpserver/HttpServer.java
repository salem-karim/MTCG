package org.mtcg.httpserver;

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
  private final ExecutorService pool = Executors.newFixedThreadPool(4);

  public HttpServer(int port) {
    this.port = port;
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
            var input = client.getInputStream();
            var output = client.getOutputStream();
            var inputReader = new InputStreamReader(input);
            var bufferReader = new BufferedReader(inputReader);
            var printWriter = new PrintWriter(output);
            // Create the request object from the client's input stream
            HttpRequest req = new HttpRequest(bufferReader);
            HttpRequestHandler handler = new HttpRequestHandler();
            handler.handleRequest(printWriter, req); // Handle the request
          } catch (IOException e) {
            System.err.println("Error handling request: " + e.getMessage());
          } finally {
            // Ensure the client socket is closed after handling the request
            try {
              client.close();
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
