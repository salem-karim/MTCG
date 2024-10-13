package org.mtcg;

public class Main {
  public static void main(String[] args) {
    System.out.println("Hello and welcome!");
    var Http = new HttpServer(10001);
    Http.run();
  }
}