// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.gson.Gson;
import com.google.sps.servlets.utils.CommentAnalysis;
import com.google.sps.servlets.utils.Statistics;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that fetches from Youtube Server. */
@WebServlet("/YouTubeComments")
public class YoutubeServlet extends HttpServlet {
  private static final Long COMMENT_LIMIT = 100L;
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String URL_PARAMETER = "url";
  // Parameters required by YouTube API to retrieve the comment threads
  private static final String SNIPPET_PARAMETERS = "snippet,replies";
  private static final String ORDER_PARAMETER = "relevance";

  private static final String APPLICATION_NAME = "SAY";
  // TODO: have dev key come from centralized location, rather than being hard-coded.
  private static final String DEVELOPER_KEY = "AIzaSyDYfjWcy1hEe0V7AyaYzgIQm_rT-9XbiGs";

  /**
   * Retrieves comments from designated URL, passes them off to CommentAnalysis object to be wrapped
   * into Statistics object, then writes the Statistics object to the frontend.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    try {
      String url = request.getParameter(URL_PARAMETER);
      CommentThreadListResponse commentResponse = generateYouTubeRequest(url).execute();

      CommentAnalysis commentAnalysis = new CommentAnalysis();
      Statistics statistics = commentAnalysis.computeOverallStats(commentResponse);
      commentAnalysis.closeLanguage();

      System.out.println("Statistics" + statistics);
      System.out.println(statistics.getAverageScore());
      System.out.println(statistics.getScores());

      String json = new Gson().toJson(statistics);
      response.setContentType("application/json");
      response.getWriter().println(json);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      throw new ServletException("Unable to fetch YouTube Comments Through Servlet.", e);
    }
  }

  /**
   * Applies parameters to comment request, then uses it to extract comments. URL is the only true
   * variable; for this application we will always want order to be relevance, and max results to be
   * 100, the API's limit for how many comments can be retrieved via a single request.
   */
  private YouTube.CommentThreads.List generateYouTubeRequest(String url)
      throws GeneralSecurityException, IOException {
    YouTube youtubeService = getService();
    YouTube.CommentThreads.List commentRequest =
        youtubeService.commentThreads().list(SNIPPET_PARAMETERS);
    return commentRequest
        .setKey(DEVELOPER_KEY)
        .setVideoId(url)
        .setOrder(ORDER_PARAMETER)
        .setMaxResults(COMMENT_LIMIT);
  }

  /**
   * Build and return an authorized API client service.
   *
   * @return an authorized API client service
   * @throws GeneralSecurityException, IOException
   */
  private static YouTube getService() throws GeneralSecurityException, IOException {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }
}
