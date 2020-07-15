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

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequest;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.sps.servlets.ContextServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.security.GeneralSecurityException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** This is a JUnit test for sentiment mockedAnalysis */
@RunWith(JUnit4.class)

public class ContextServletTest {
  private static final String URL_PARAMETER = "url";
  private static final String TEST_URL = "WkZ5e94QnWk";
  private static final String REQUEST_INFO = "snippet,contentDetails,statistics";

  @Test
  public void testContextGet() throws ServletException, IOException, GeneralSecurityException {
    ContextServlet contextServlet = new ContextServlet();
    HttpServletRequest mockedRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockedResponse = mock(HttpServletResponse.class);
    when(mockedRequest.getParameter(URL_PARAMETER)).thenReturn(TEST_URL);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(mockedResponse.getWriter()).thenReturn(writer);
    contextServlet.doGet(mockedRequest,mockedResponse);
    verify(mockedRequest, atLeast(1)).getParameter(URL_PARAMETER); // only if you want to verify username was called...
    Assert.assertTrue(stringWriter.toString().contains("Fuck Donald Trump"));
    Assert.assertTrue(stringWriter.toString().contains("\"videoAuthor\":\"WORLDSTARHIPHOP\""));
    Assert.assertTrue(stringWriter.toString().contains("numLikes"));
    Assert.assertTrue(stringWriter.toString().contains("numDislikes"));
    Assert.assertTrue(stringWriter.toString().contains("\"publishDate\":\"2016-04-18T17:01:23.000Z\""));
  }
}
