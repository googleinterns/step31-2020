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
import com.google.sps.servlets.utils.Range;
import com.google.sps.servlets.utils.Statistics;
import java.io.IOException;
import java.security.GeneralSecurityException;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * This is a JUnit test for sentiment analysis
 */
@RunWith(JUnit4.class)
public class CommentAnalysisTest {
  private CommentThreadListResponse youtuberesponse;
  private CommentAnalysis analysis;
  private static final String APPLICATION_NAME = "testComment";
  private static final String DEVELOPER_KEY = "AIzaSyDLE0TsAmPxbF_D_t3J4-aqBuFKs4chMgM";
  private static final String PLAINTEXT = "plainText";
  private static final String SNIPPET = "snippet";
  private static final String TEST_VIDEO_ID = "E_wKLOq-30M";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final ArrayList<Double> SCOREVALUE_1 = new ArrayList<>(Arrays.asList(0.001, 0.002, 0.003, 0.005, -0.1, -0.2));
  private static final ArrayList<Double> SCOREVALUE_2 = new ArrayList<>(Arrays.asList(1.0, -1.0, 0.0));
  private static final ArrayList<Double> SCOREVALUE_3 = new ArrayList<>(Arrays.asList(0.5, 0.9,  -0.5, -0.9));
  private static final ArrayList<Double> SCOREVALUE_4 = new ArrayList<>(Arrays.asList(-2.0, -3.0, 3.0, -100.2));
  Statistics testStat_1 = new Statistics(SCOREVALUE_1);
  Statistics testStat_2 = new Statistics(SCOREVALUE_2);
  Statistics testStat_3 = new Statistics(SCOREVALUE_3);
  Statistics testStat_4 = new Statistics(SCOREVALUE_4);

  /**
   * Build and return an authorized API client service.
   *
   * @return an authorized API client service
   * @throws GeneralSecurityException, IOException
   */
  public static YouTube getService() throws GeneralSecurityException, IOException {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
               .setApplicationName(APPLICATION_NAME).build();
  }

  @Before
  public void setUp() throws GeneralSecurityException, IOException{
    YouTube youtubeService = getService();
    YouTube.CommentThreads.List youtuberequest = youtubeService.commentThreads().list(SNIPPET);
    youtuberesponse = youtuberequest.setKey(DEVELOPER_KEY).setVideoId(TEST_VIDEO_ID)
                                      .setMaxResults(2L).setTextFormat(PLAINTEXT).execute();
    analysis = new CommentAnalysis(youtuberesponse);
  }

  @Test
  public void testSentimentAnalysisInRange() {
    // Test the Sentiment Analysis Score within range -1 to 1.
    Statistics result = analysis.computeOverallStats();
    Assert.assertTrue(result.getAverageScore() >= -1 && result.getAverageScore() <= 1);
  }

  @Test
  public void testCategorizationEdgeCases() {
    Assert.assertEquals(testStat_1.getAggregateValues().get(new Range(0,0.2)).intValue(), 4);
    Assert.assertEquals(testStat_1.getAggregateValues().get(new Range(-0.2,0)).intValue(), 2);
    Assert.assertEquals(testStat_2.getAggregateValues().get(new Range(-1.0,-0.8)).intValue(), 1);
    Assert.assertEquals(testStat_2.getAggregateValues().get(new Range(0.8,1)).intValue(), 1);
    Assert.assertEquals(testStat_4.getAggregateValues().get(new Range(-1.0,-0.8)).intValue(), 0);
    Assert.assertEquals(testStat_4.getAggregateValues().get(new Range(0.8,1.0)).intValue(), 0);
  }

  @Test
  public void testAvgScore() {
    Assert.assertTrue(testStat_2.getAverageScore() == 0.0);
    Assert.assertTrue(testStat_3.getAverageScore() == 0.0);
    Assert.assertTrue(testStat_4.getAverageScore() == 0.0);
  }
}
