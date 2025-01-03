package org.mtcg.httpserver;

import lombok.Getter;

import org.mtcg.db.UserDbAccess;
import org.mtcg.models.User;
import org.mtcg.utils.Method;
import org.mtcg.utils.HttpRequestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Getter
public class HttpRequest {
  private final Method method;
  private final String path;
  private final List<String> pathSegments;
  private final String body;
  private final Map<String, String> headers;
  private final User user;
  private final UserDbAccess userDbAccess;

  // gets the Route in 1 String for Router class
  public String getServiceRoute() {
    if (this.pathSegments == null || this.pathSegments.isEmpty()) {
      return null;
    } else {
      return "/" + String.join("/", this.pathSegments);
    }
  }

  public HttpRequest(final BufferedReader reader, final UserDbAccess userdbAccess) throws HttpRequestException {
    this.userDbAccess = userdbAccess;
    this.headers = new HashMap<>();

    try {
      // Read the request line
      final String requestLine = reader.readLine();
      if (requestLine != null) {
        final String[] parts = requestLine.split(" ");
        if (parts.length >= 2) {
          // e.g., "POST"
          this.method = Method.valueOf(parts[0].toUpperCase(Locale.ROOT));
          // e.g., "/packages"
          this.path = parts[1];
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
      String headerLine;
      while (!(headerLine = reader.readLine()).isEmpty()) {
        final String[] headerParts = headerLine.split(": ", 2);
        if (headerParts.length == 2) {
          // Store header in HashMap
          headers.put(headerParts[0], headerParts[1]);
        }
      }

      // Read body if Content-Length is present
      final String contentLengthHeader = headers.get("Content-Length");
      if (contentLengthHeader != null) {
        final int contentLength = Integer.parseInt(contentLengthHeader);
        // read body from the buffer which is as long as contentlength
        final char[] bodyBuffer = new char[contentLength];
        final int charsRead = reader.read(bodyBuffer, 0, contentLength);
        // Verify that the whole Body is read
        if (charsRead != contentLength) {
          throw new IOException("Failed to read the full request body");
        }
        this.body = new String(bodyBuffer);
      } else {
        // No body if Content-Length is absent
        this.body = "";
      }

      // "Middleware" gets the token of header and uses DB Access Method
      final String authorization = headers.get("Authorization");
      if (authorization != null && authorization.startsWith("Bearer ")) {
        final String token = authorization.substring(7);
        this.user = userDbAccess.getUserFromToken(token); // Set the user object
      } else {
        user = null;
      }
    } catch (IOException | NumberFormatException e) {
      throw new HttpRequestException("Error parsing HTTP request", e);
    }
  }

  // Constructor for Unit tests and dependency ingection
  public HttpRequest(final Method method, final String path, final String body, final String token) {
    this(method, path, body, new HashMap<>(), token, new UserDbAccess());
  }

  public HttpRequest(final Method method, final String path, final String body, final Map<String, String> headers,
      final String token, final UserDbAccess userdbAccess) {
    this.method = method;
    this.path = path;
    this.body = body;
    this.headers = headers != null ? headers : new HashMap<>();
    this.pathSegments = new ArrayList<>();

    this.userDbAccess = userdbAccess;
    this.user = userDbAccess.getUserFromToken(token);

    // Split path into segments
    final String[] pathParts = path.split("/");
    for (final String part : pathParts) {
      if (!part.isEmpty()) {
        this.pathSegments.add(part);
      }
    }
  }

}
