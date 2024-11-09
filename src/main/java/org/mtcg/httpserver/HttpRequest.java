package org.mtcg.httpserver;

import lombok.Getter;
import org.mtcg.utils.Method;
import org.mtcg.utils.exceptions.HttpRequestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

@Getter
public class HttpRequest {
  private final Method method;
  private final String path;
  private final List<String> pathSegments;
  private final String body;
  private final Map<String, String> headers;

  public String getServiceRoute() {
    if (this.pathSegments == null || this.pathSegments.isEmpty()) {
      return null;
    } else {
      return '/' + this.pathSegments.getFirst();
    }
  }

  public HttpRequest(final BufferedReader reader) throws HttpRequestException {
    this.headers = new HashMap<>(); // Initialize the headers map

    try {
      // Read the request line
      final String requestLine = reader.readLine();
      if (requestLine != null) {
        final String[] parts = requestLine.split(" ");
        if (parts.length >= 2) {
          this.method = Method.valueOf(parts[0].toUpperCase(Locale.ROOT)); // e.g., "POST"
          this.path = parts[1]; // e.g., "/packages"
        } else {
          throw new IOException("Invalid HTTP request line");
        }
      } else {
        throw new IOException("Empty request line");
      }
      this.pathSegments = new ArrayList<>();
      final String[] pathParts = this.path.split("/");
      for (final String part : pathParts) {
        if (!part.isEmpty()) {
          this.pathSegments.add(part);
        }
      }
      // Read headers
      // TODO: Check if it reads all headers
      String headerLine;
      while (!(headerLine = reader.readLine()).isEmpty()) {
        final String[] headerParts = headerLine.split(": ", 2);
        if (headerParts.length == 2) {
          headers.put(headerParts[0], headerParts[1]); // Store header
        }
      }

      // Read body if Content-Length is present
      final String contentLengthHeader = headers.get("Content-Length");
      if (contentLengthHeader != null) {
        final int contentLength = Integer.parseInt(contentLengthHeader);
        final char[] bodyBuffer = new char[contentLength];
        final int charsRead = reader.read(bodyBuffer, 0, contentLength);
        if (charsRead != contentLength) {
          throw new IOException("Failed to read the full request body");
        }
        this.body = new String(bodyBuffer);
      } else {
        this.body = ""; // No body if Content-Length is absent
      }
    } catch (IOException | NumberFormatException e) {
      throw new HttpRequestException("Error parsing HTTP request", e);
    }
  }
}
