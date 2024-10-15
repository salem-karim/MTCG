package org.mtcg.httpserver;

public class HttpRequestHandler {
  /*
  Have a public Object and Object Service which then
  gets initialized by an ObjectMapper to a certain Object
  which is in the Request itself and the handle the certain request
 POLYMORPHISM
   */
  public void handleRequest() {
    System.out.println("Request handled");
  }
}
