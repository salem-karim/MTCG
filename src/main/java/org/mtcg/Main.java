package org.mtcg;

import org.mtcg.httpserver.HttpServer;

public class Main {
  public static void main(String[] args) {
    var Http = new HttpServer(10001);
    Http.run();
  }
}