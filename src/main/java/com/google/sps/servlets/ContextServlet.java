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
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.gson.Gson;
import com.google.sps.servlets.utils.VideoInformation;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that fetches from Youtube Server. */
@WebServlet("/VideoContext")
public class ContextServlet extends HttpServlet {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String URL_PARAMETER = "url";
  // Parameters required by YouTube API to retrieve the comment threads
  // TODO: encapsulate this piece into a YoutubeProvider class to avoid repeating the code
  private static final String APPLICATION_NAME = "SAY";
  private static final String DEVELOPER_KEY = "AIzaSyDYfjWcy1hEe0V7AyaYzgIQm_rT-9XbiGs";
  private static final String REQUEST_INFO = "snippet,statistics";
  private YouTube youtubeService;

  /**
   * Constructor for mocking servlet to call the doGet.
   * @throws GeneralSecurityException
   * @throws IOException
   */
  public ContextServlet() throws GeneralSecurityException, IOException {
    youtubeService = getService();
  }

  /**
   * Constructor for mocking and testing constructExecuteYouTubeRequest with Youtube
    * @param constructedService mocked & pre-constructed Youtube Service
   */
  public ContextServlet(YouTube constructedService) {
    youtubeService = constructedService;
  }

  @Override
  public void init() throws ServletException {
    try {
      youtubeService = getService();
    } catch (GeneralSecurityException | IOException e) {
      throw new ServletException(e.getMessage());
    }
  }
  /**
   * Retrieves video information from designated URL, wraps them into information object, then
   * writes the VideoInformation object to the frontend.
   *
   * @param request request from our web server
   * @param response empty response to write json object
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException {
    try {
      String url = request.getParameter(URL_PARAMETER);
      VideoListResponse videoResponse = constructExecuteYouTubeRequest(url);
      VideoInformation videoInfo = new VideoInformation(videoResponse);
      String json = new Gson().toJson(videoInfo);
      response.setContentType("application/json");
      response.getWriter().println(json);
    } catch (Exception e) {
      e.printStackTrace(System.err);
      throw new ServletException("Unable to fetch YouTube Video Information Through Servlet.", e);
    }
  }

  /**
   * Connect to Youtube Server and generate request to retrieve video information
   *
   * @param url Youtube video id to retrieve information
   */
  public VideoListResponse constructExecuteYouTubeRequest(String url) throws IOException {
    YouTube.Videos.List videoRequest = youtubeService.videos().list(REQUEST_INFO);
    return videoRequest.setKey(DEVELOPER_KEY).setId(url).execute();
  }

  /**
   * Build and return an authorized API client service.
   *
   * @return an authorized API client service
   * @throws GeneralSecurityException, IOException
   */
  private YouTube getService() throws GeneralSecurityException, IOException {
    // TODO: Encapsulate this getService() method with comment retrieving service once refactored
    // YoutubeServlet gets merged
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }
}
