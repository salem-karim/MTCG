package org.mtcg.httpserver;

import java.io.*;
import java.net.ServerSocket;
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
        var client = socket.accept();
        System.out.println("HTTP Server accepted connection from " + client.getRemoteSocketAddress());
        pool.execute(() -> {
          try {
            final var handler = new HttpRequestHandler(client);
            //final var handler = new HttpRequestHandler(new HttpRequest(...), client);
            handler.handleRequest();
          } catch (RuntimeException e) {
            throw new RuntimeException(e);
          }
        });
      }
    } catch (Exception e) {
      System.err.println(e);
    } finally {
      close();
    }
  }

  public void close() {
    System.out.println("HTTP Server closing on port " + port);
    try {
      pool.shutdown();
      if (!pool.awaitTermination(60, TimeUnit.SECONDS))
        pool.shutdownNow();
      socket.close();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
