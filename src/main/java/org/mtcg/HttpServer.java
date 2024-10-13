package org.mtcg;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer implements Runnable {
  private static ServerSocket socket  = null;
  private final int port;

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
        // do the rest
      }
    } catch ( Exception e ) {
      System.err.println( e );
    }
    finally {
      close();
    }
  }
  public void close() {
    System.out.println("HTTP Server closing on port " + port);
    try {
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
