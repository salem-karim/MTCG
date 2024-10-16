package org.mtcg.httpserver;

import java.io.*;
import java.net.Socket;

public class HttpRequestHandler {
  public void handleRequest(Socket socket, HttpRequest request) throws IOException {
    // Log the request details
    System.out.println("Request Method: " + request.getMethod());
    System.out.println("Request Path: " + request.getPath());
    System.out.println("Request Headers: " + request.getHeaders());
    System.out.println("Request Body: " + request.getBody());

    // Prepare the response
    String response = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/plain\r\n" +
            "Content-Length: 2\r\n" +
            "\r\n" +
            "OK";

    // Send the response to the client
    try (OutputStream outputStream = socket.getOutputStream()) {
      outputStream.write(response.getBytes());
      outputStream.flush();
    } catch (IOException e) {
      System.err.println("Error writing response: " + e.getMessage());
    }

  }

}
