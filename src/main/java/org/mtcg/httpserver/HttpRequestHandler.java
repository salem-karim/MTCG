package org.mtcg.httpserver;

import java.io.*;
import java.net.Socket;

public class HttpRequestHandler {
  private Socket socket;
  // private HttpRequest request;
  public HttpRequestHandler(/*HttpRequest req, */Socket client) {
    socket = client;
    //request = req;
  }

  public void handleRequest() {
    try {
      var input = socket.getInputStream();
      var bufferReader = new BufferedReader(new InputStreamReader(input));
      String line;
      int contentLength = 0;

      // Reading headers
      while (!(line = bufferReader.readLine()).isEmpty()) {
        System.out.println(line);

        // Capture Content-Length to determine body length
        if (line.toLowerCase().startsWith("content-length:")) {
          contentLength = Integer.parseInt(line.split(":")[1].trim());
        }
      }

      // If there's a body, read it based on Content-Length
      if (contentLength > 0) {
        char[] body = new char[contentLength];
        bufferReader.read(body, 0, contentLength);
        String requestBody = new String(body);
        System.out.println("Body: " + requestBody);
      }

    } catch (IOException e) {
      System.err.println("Error handling request: " + e.getMessage());
    }
  }
}
