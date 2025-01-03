package org.mtcg.controllers;

import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.UserStats;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;

public class StatController extends Controller {

  public HttpResponse listUsersStats(final HttpRequest request) {
    final var user = request.getUser();
    if (user == null) {
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    } else {
      try {
        // Make a UserStats Object from User from request
        final var userStats = new UserStats(user.getUsername(), user.getElo(), user.getWins(), user.getLosses());
        return new HttpResponse(HttpStatus.OK, ContentType.JSON, getObjectMapper().writeValueAsString(userStats));
      } catch (final JsonProcessingException e) {
        return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
            createJsonMessage("error", "User not fetched correctly"));
      }
    }
  }
}
