package org.mtcg.httpserver;

import java.io.*;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mtcg.services.Service;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.Router;
import org.mtcg.utils.exceptions.HttpRequestException;

public class HttpRequestHandler implements Runnable {
  private static final Logger logger = Logger.getLogger(HttpRequestHandler.class.getName());
  private final Socket clientSocket;
  private final Router router;

  public HttpRequestHandler(Socket clientSocket, Router router) {
    this.clientSocket = clientSocket;
    this.router = router;
  }

  @Override
  public void run() {
    try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

      HttpRequest request = parseRequest(reader);
      handleRequest(writer, request);

    } catch (IOException | HttpRequestException e) {
      logger.log(Level.SEVERE, "Error handling request", e);
    } finally {
      try {
        clientSocket.close();
      } catch (IOException e) {
        logger.log(Level.SEVERE, "Error closing client socket", e);
      }
    }
  }

  private HttpRequest parseRequest(BufferedReader reader) throws HttpRequestException {
    return new HttpRequest(reader);
  }

  public void handleRequest(PrintWriter writer, HttpRequest request) throws IOException {
    System.out.println("Received request:");
    System.out.println("Method: " + request.getMethod());
    System.out.println("Path: " + request.getPath());
    System.out.println("Headers: " + request.getHeaders());
    System.out.println("Body: " + request.getBody());

    HttpResponse response;
    if (request.getPath() == null) {
      response = new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON, "");
    } else if ("/".equals(request.getPath())) {
      // To Test basic functionality
      response =
          new HttpResponse(
              HttpStatus.OK,
              ContentType.HTML,
              "<html><body>Welcome to the homepage!</body></html>");
    } else {
      Service service = this.router.resolve(request.getServiceRoute());
      if (service != null) {
        response = service.handle(request);
      } else {
        response =
            new HttpResponse(HttpStatus.NOT_FOUND, ContentType.JSON, "{\"error\":\"Not Found\"}");
      }
    }
    writer.write(response.getResponse());
    writer.flush();
  }
}
