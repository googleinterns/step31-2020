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
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.sps.servlets.utils.CommentAnalysis;
import com.google.sps.servlets.utils.Statistics;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** This is a JUnit test for sentiment analysis */
@RunWith(JUnit4.class)
public class CommentAnalysisTest {
  private CommentThreadListResponse youtuberesponse;
  private CommentAnalysis analysis;
  private static final String APPLICATION_NAME = "testComment";
  private static final String DEVELOPER_KEY = "API_KEY";
  private static final String PLAINTEXT = "plainText";
  private static final String SNIPPET = "snippet";
  private static final String TEST_VIDEO_ID = "E_wKLOq-30M";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /**
   * Build and return an authorized API client service.
   *
   * @return an authorized API client service
   * @throws GeneralSecurityException, IOException
   */
 /* public static YouTube getService() throws GeneralSecurityException, IOException {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  @Before
  public void setUp() throws GeneralSecurityException, IOException {
    YouTube youtubeService = getService();
    YouTube.CommentThreads.List youtuberequest = youtubeService.commentThreads().list(SNIPPET);
    youtuberesponse =
        youtuberequest
            .setKey(DEVELOPER_KEY)
            .setVideoId(TEST_VIDEO_ID)
            .setMaxResults(2L)
            .setTextFormat(PLAINTEXT)
            .execute();
    analysis = new CommentAnalysis();
  }

  @Test
  public void testSentimentAnalysisInRange() {
    // Test the Sentiment Analysis Score within range -1 to 1.
    Statistics result = analysis.computeOverallStats(youtuberesponse);
    Assert.assertTrue(Math.abs(result.getAverageScore()) <= 1);
  }
*/
  @Test
  public void testCategorizationEdgeCases() {
    // TODO: test for splitting different scores into intervals
  }
}
