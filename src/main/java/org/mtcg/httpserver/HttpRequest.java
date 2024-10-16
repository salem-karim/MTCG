package org.mtcg.httpserver;

import lombok.Getter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


@Getter
public class HttpRequest {
  private final String method;
  private final String path;
  private final String body;
  private final Map<String, String> headers;

  public HttpRequest(InputStream inputStream) throws IOException {
    this.headers = new HashMap<>(); // Initialize the headers map

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      // Read the request line
      String requestLine = reader.readLine();
      if (requestLine != null) {
        String[] parts = requestLine.split(" ");
        if (parts.length >= 2) {
          this.method = parts[0]; // e.g., "POST"
          this.path = parts[1];   // e.g., "/packages"
        } else {
          throw new IOException("Invalid HTTP request line");
        }
      } else {
        throw new IOException("Empty request line");
      }

      // Read headers
      String headerLine;
      while (!(headerLine = reader.readLine()).isEmpty()) {
        String[] headerParts = headerLine.split(": ", 2);
        if (headerParts.length == 2) {
          headers.put(headerParts[0], headerParts[1]); // Store header
        }
      }

      // Read body if Content-Length is present
      String contentLengthHeader = headers.get("Content-Length");
      if (contentLengthHeader != null) {
        int contentLength = Integer.parseInt(contentLengthHeader);
        char[] bodyBuffer = new char[contentLength];
        reader.read(bodyBuffer, 0, contentLength);
        this.body = new String(bodyBuffer);
      } else {
        this.body = ""; // No body if Content-Length is absent
      }
    }
  }
}
