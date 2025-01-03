package org.mtcg.controllers;

import java.util.List;

import org.mtcg.db.UserDbAccess;
import org.mtcg.httpserver.HttpRequest;
import org.mtcg.httpserver.HttpResponse;
import org.mtcg.models.UserStats;
import org.mtcg.utils.ContentType;
import org.mtcg.utils.HttpStatus;
import org.mtcg.utils.HttpRequestException;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ScoreBoardController extends Controller {
  private final UserDbAccess userDbAccess;

  public ScoreBoardController() {
    super();
    this.userDbAccess = new UserDbAccess();
  }

  public HttpResponse listScoreBoard(final HttpRequest request) {
    try {
      if (request.getUser() == null) {
        throw new HttpRequestException("User not Authorized");
      }
      // Get the stats from All Users and format into JSON
      final List<UserStats> allUserStats = userDbAccess.getAllUserStats();
      if (allUserStats == null) {
        return new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON,
            createJsonMessage("error", "Error while retrieving all User Stats"));
      } else if (allUserStats.isEmpty()) {
        return new HttpResponse(HttpStatus.NO_CONTENT, ContentType.JSON, "\n");
      } else {
        return new HttpResponse(HttpStatus.OK, ContentType.JSON,
            getObjectMapper().writeValueAsString(allUserStats));
      }
    } catch (final HttpRequestException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.UNAUTHORIZED, ContentType.JSON,
          createJsonMessage("error", "Access token is missing or invalid"));
    } catch (final JsonProcessingException e) {
      System.out.println(e.getMessage());
      return new HttpResponse(HttpStatus.BAD_REQUEST, ContentType.JSON,
          createJsonMessage("error", "Serialisation Error"));
    }
  }

}
