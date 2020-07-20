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
package com.google.sps;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.google.sps.servlets.ContextServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** This is a JUnit test for sentiment mockedAnalysis */
@RunWith(JUnit4.class)
public class ContextServletTest {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final String URL_PARAMETER = "url";
  private static final String TEST_URL = "WkZ5e94QnWk";
  private static final String TEST_TITLE = "Test Title";
  private static final String TEST_CHANNEL = "Test Channel";
  private static final String REQUEST_INFO = "snippet,statistics";
  private static final int NUM_LIKES_VAL = 10;
  private static final int NUM_DISLIKES_VAL = 1;

  @Test
  public void testYoutubeAPIConstructed() throws IOException, GeneralSecurityException {
    // Test the constructeExecuteYoutubeReuest method for returning a youtube request
    YouTube youtubeService =
        new YouTube(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null);
    YouTube mockedYoutubeService = spy(youtubeService);
    YouTube.Videos mockedVidoes = mock(YouTube.Videos.class);
    YouTube.Videos.List mockedVideoList = mock(YouTube.Videos.List.class, RETURNS_DEEP_STUBS);
    ContextServlet contestServlet = new ContextServlet(mockedYoutubeService);
    when(mockedVidoes.list(REQUEST_INFO)).thenReturn(mockedVideoList);
    when(mockedYoutubeService.videos()).thenReturn(mockedVidoes);
    when(mockedVideoList.execute()).thenReturn(new VideoListResponse());
    VideoListResponse response = contestServlet.constructAndExecuteYoutubeRequest(TEST_URL);
    Assert.assertEquals(response.getClass(), VideoListResponse.class);
    Assert.assertEquals(response.getItems().size(), 0);
  }

  @Test
  public void testContextGet_generateExpectedResponse() throws IOException, GeneralSecurityException, ServletException {
    // Test the doGet() method to retrieve youtube vidoe context with mocked service
    ContextServlet contextServlet = spy(ContextServlet.class);
    HttpServletRequest mockedRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockedResponse = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    VideoSnippet mockedVideoSnippet =
        new VideoSnippet()
            .setPublishedAt(new DateTime(new Date()))
            .setTitle(TEST_TITLE)
            .setChannelTitle(TEST_CHANNEL);
    VideoStatistics mockedVidoeStatistics =
        new VideoStatistics().setLikeCount(BigInteger.TEN).setDislikeCount(BigInteger.ONE);
    Video mockedVideo =
        new Video().setSnippet(mockedVideoSnippet).setStatistics(mockedVidoeStatistics);
    VideoListResponse mockedVideoList =
        new VideoListResponse().setItems(Collections.singletonList(mockedVideo));
    when(mockedRequest.getParameter(URL_PARAMETER)).thenReturn(TEST_URL);
    when(contextServlet.constructAndExecuteYoutubeRequest(TEST_URL)).thenReturn(mockedVideoList);
    when(mockedResponse.getWriter()).thenReturn(writer);
    contextServlet.doGet(mockedRequest, mockedResponse);
    verify(mockedRequest, atLeast(1)).getParameter(URL_PARAMETER);
    Assert.assertTrue(stringWriter.toString().contains("\"videoName\":\"" + TEST_TITLE + "\""));
    Assert.assertTrue(stringWriter.toString().contains("\"videoAuthor\":\"" + TEST_CHANNEL + "\""));
    Assert.assertTrue(stringWriter.toString().contains("\"numLikes\":" + NUM_LIKES_VAL));
    Assert.assertTrue(stringWriter.toString().contains("\"numDislikes\":" + NUM_DISLIKES_VAL));
    Assert.assertTrue(stringWriter.toString().contains("\"publishDateString\":"));
  }
}
