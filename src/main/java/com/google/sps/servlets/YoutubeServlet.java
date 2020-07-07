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
import com.google.sps.servlets.utils.YouTubeCommentRetriever;
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
  private static final String URL_PARAMETER = "url";

  /**
   * Retrieves comments from designated URL, passes them off to CommentAnalysis object to be wrapped
   * into Statistics object, then writes the Statistics object to the frontend.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    try {
      String url = request.getParameter(URL_PARAMETER);
      // TODO: extract comments from YouTubeCommentRetriever
      CommentThreadListResponse commentResponse = null; //= generateYouTubeRequest(url).execute();

      CommentAnalysis commentAnalysis = new CommentAnalysis();
      Statistics statistics = commentAnalysis.computeOverallStats(commentResponse);
      commentAnalysis.closeLanguage();

      String json = new Gson().toJson(statistics);
      response.setContentType("application/json");
      response.getWriter().println(json);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      throw new ServletException("Unable to fetch YouTube Comments Through Servlet.", e);
    }
  }

}
