package org.mtcg.httpserver;

import java.io.*;

public class HttpRequestHandler {
  public void handleRequest(PrintWriter writer, HttpRequest request) throws IOException {
    // Log the request details
    System.out.println("Request Method: " + request.getMethod());
    System.out.println("Request Path: " + request.getPath());
    System.out.println("Request Headers: " + request.getHeaders());
    System.out.println("Request Body: " + request.getBody());

    // Prepare the response
    HttpResponse response = new HttpResponse(200, "User created");
    // Send the response to the client
    writer.write(response.getResponse());
    writer.flush();
  }
}
