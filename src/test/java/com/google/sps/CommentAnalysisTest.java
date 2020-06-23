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
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.sps.servlets.CommentAnalysis;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public class CommentAnalysisTest {
  private CommentThread testCommentThread;
  private static final String DEVELOPER_KEY = "";
  private static final String APPLICATION_NAME = "API code samples";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /**
   * Build and return an authorized API client service.
   *
   * @return an authorized API client service
   * @throws GeneralSecurityException, IOException
   */
  public static YouTube getService() throws GeneralSecurityException, IOException {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
               .setApplicationName(APPLICATION_NAME)
               .build();
  }

  @Before
  public void setUp() {
    try {
      YouTube youtubeService = getService();
      YouTube.CommentThreads.List youtuberequest = youtubeService.commentThreads().list("snippet");
      CommentThreadListResponse youtuberesponse = youtuberequest.setKey(DEVELOPER_KEY)
                                                      .setVideoId("31dYohFK0Tc")
                                                      .setMaxResults(2L)
                                                      .setOrder("time")
                                                      .setTextFormat("plainText")
                                                      .execute();
      testCommentThread = youtuberesponse.getItems().get(0);
    } catch (GeneralSecurityException | IOException e) {
      return;
    }
  }

  @Test
  public void testSentimentAnalysis() {
    CommentAnalysis analysis = new CommentAnalysis(testCommentThread);
    try {
      System.out.println(analysis.sentiAnalysis());
    } catch (IOException e) {
      System.out.println("failed");
    }

  }
}
