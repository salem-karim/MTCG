package org.mtcg.httpserver;

import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.Router;

import java.io.*;

public class HttpRequestHandler {
  private final Router router;

  // Constructor to initialize the router
  public HttpRequestHandler(Router router) {
    this.router = router;
  }

  public void handleRequest(PrintWriter writer, HttpRequest request) throws IOException {
    // Log the request details
    System.out.println("Request Method: " + request.getMethod());
    System.out.println("Request Path: " + request.getPath());
    System.out.println("Request Headers: " + request.getHeaders());
    System.out.println("Request Body: " + request.getBody());

    // Prepare the response
    HttpResponse response;
    if (request.getPath() == null) {
      response = new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "");
    } else {
      response = this.router.resolve(request.getServiceRoute()).handle(request);
    }
    // Send the response to the client
    writer.write(response.getResponse());
    writer.flush();
  }
}
