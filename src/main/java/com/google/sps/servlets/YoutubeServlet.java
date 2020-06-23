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
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThreadListResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that fetch from Youtube Server.
 */
@WebServlet("/YouTubeComments")
public class YoutubeServlet extends HttpServlet {
  private static final String DEVELOPER_KEY = "OUR_API_KEY"; //TODO: obtain actual API Key

  private static final String APPLICATION_NAME = "SAY";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    try {
      System.err.println("Hello world!");
      YouTube youtubeService = getService();
      String url = request.getParameter("url");
      // Define and execute the API request
      YouTube.CommentThreads.List commentRequest = youtubeService.commentThreads()
          .list("snippet,replies");
      CommentThreadListResponse commentResponse = commentRequest.setKey(DEVELOPER_KEY)
          .setVideoId(url)
          .setOrder("relevance")
          .setMaxResults(100L)
          .execute();
      response.getWriter().println(commentResponse);
    } catch(GeneralSecurityException | GoogleJsonResponseException e) { 
        System.err.println("Error in retrieving YouTube Comments!");
        System.err.println(e.getMessage());
    }
  }

  /**
   * Build and return an authorized API client service.
   *
   * @return an authorized API client service
   * @throws GeneralSecurityException, IOException
   * 
   */
  public static YouTube getService() throws GeneralSecurityException, IOException {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }
}
