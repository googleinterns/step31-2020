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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.api.services.youtube.model.CommentThreadSnippet;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.sps.servlets.utils.CommentAnalysis;
import com.google.sps.servlets.utils.Range;
import com.google.sps.servlets.utils.Statistics;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** This is a JUnit test for sentiment mockedAnalysis */
@RunWith(JUnit4.class)
public class CommentAnalysisTest {
  private static final float testScore = 0.23f;
  private static final ArrayList<Double> SCORE_IN_RANGE =
      new ArrayList<>(Arrays.asList(0.001, 0.002, 0.003, 0.005, -0.1, -0.2));
  private static final ArrayList<Double> EDGE_SCORE =
      new ArrayList<>(Arrays.asList(1.0, -1.0, 0.0));
  private static final ArrayList<Double> SYMMETRIC_SCORE =
      new ArrayList<>(Arrays.asList(0.5, 0.9, -0.5, -0.9));
  private static final ArrayList<Double> ALL_OUTSIDE_SCORE =
      new ArrayList<>(Arrays.asList(-2.0, -3.0, 3.0, -100.2));
  private static final ArrayList<Double> ONE_OUSIDE_SCORE =
      new ArrayList<>(Arrays.asList(-2.0, -1.0, 0.0));
  private static final Statistics NORMAL_STAT = new Statistics(SCORE_IN_RANGE);
  private static final Statistics EDGE_STAT = new Statistics(EDGE_SCORE);
  private static final Statistics SYMMETRIC_STAT = new Statistics(SYMMETRIC_SCORE);
  private static final Statistics ONE_OUSIDE_STAT = new Statistics(ONE_OUSIDE_SCORE);
  private static Comment testTopComment = new Comment();
  private static CommentSnippet topCommentSnippet = new CommentSnippet();
  private static CommentThread testCommentThread = new CommentThread();
  private static List<CommentThread> testCommentThreadLst =
      new ArrayList<>(Arrays.asList(testCommentThread, testCommentThread));
  private static CommentThreadListResponse youtubeResponse = new CommentThreadListResponse();
  private static CommentThreadSnippet testThreadSnippet = new CommentThreadSnippet();
  private static LanguageServiceClient mockedlanguageService =
      mock(LanguageServiceClient.class, Mockito.RETURNS_DEEP_STUBS);
  private static CommentAnalysis commentAnalysis = new CommentAnalysis(mockedlanguageService);

  @Before
  public void setUp() {
    topCommentSnippet.setTextDisplay("Test Message");
    testTopComment.setSnippet(topCommentSnippet);
    testThreadSnippet.setTopLevelComment(testTopComment);
    testCommentThread.setSnippet(testThreadSnippet);
    youtubeResponse.setItems(testCommentThreadLst);
  }

  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testCalculateSentiment() {
    // This is a test method to calculate simulate and test the process in comment analysis language
    // service
//    when(mockedlanguageService
//             .analyzeSentiment(any(Document.class))
//             .getDocumentSentiment()
//             .getScore())
//        .thenReturn(testScore);
//    Statistics testStat = commentAnalysis.computeOverallStats(youtubeResponse);
//    Assert.assertNotNull(testStat);
//    Assert.assertNotNull(testStat.getAggregateValues());
//    Assert.assertEquals(testStat.getAverageScore(), 0.23, 0.01);
//    Assert.assertEquals(testStat.getAverageScore(), 0.23, 0.01);
//    Assert.assertEquals(
//        testStat
//            .getAggregateValues()
//            .get(new Range(BigDecimal.valueOf(0.2), BigDecimal.valueOf(0.4)))
//            .intValue(),
//        2);
  }

  @Test
  public void testSentimentAnalysisInRange() {
    // Test the Sentiment Analysis Score within range -1 to 1.
    Assert.assertTrue(Math.abs(NORMAL_STAT.getAverageScore()) <= 1);
    Assert.assertTrue(Math.abs(EDGE_STAT.getAverageScore()) <= 1);
  }

  @Test
  public void testCategorizationEdgeCases() {
    Assert.assertEquals(
        EDGE_STAT
            .getAggregateValues()
            .get(new Range(BigDecimal.valueOf(-1.0), BigDecimal.valueOf(-0.8)))
            .intValue(),
        1);
    Assert.assertEquals(
        EDGE_STAT
            .getAggregateValues()
            .get(new Range(BigDecimal.valueOf(0.8), BigDecimal.valueOf(1.0)))
            .intValue(),
        1);
  }

  @Test
  public void testCategorizationNormalCases() {
    Assert.assertEquals(
        NORMAL_STAT
            .getAggregateValues()
            .get(new Range(BigDecimal.valueOf(0.0), BigDecimal.valueOf(0.2)))
            .intValue(),
        4);
    Assert.assertEquals(
        NORMAL_STAT
            .getAggregateValues()
            .get(new Range(BigDecimal.valueOf(-0.2), BigDecimal.valueOf(0.0)))
            .intValue(),
        2);
  }

  @Test
  public void testCategorizationOutsiderCases() {
    Assert.assertEquals(
        ONE_OUSIDE_STAT
            .getAggregateValues()
            .get(new Range(BigDecimal.valueOf(-1.0), BigDecimal.valueOf(-0.8)))
            .intValue(),
        1);
  }

  @Test
  public void testAvgNormalScore() {
    Assert.assertEquals(SYMMETRIC_STAT.getAverageScore(), 0.0, 0);
  }

  @Test
  public void testAvgEdgeScore() {
    Assert.assertEquals(EDGE_STAT.getAverageScore(), 0.0, 0);
  }

  @Test
  public void testAvgAllOutsiderScore() {
    exception.expect(RuntimeException.class);
    Statistics all_outside_stat = new Statistics(ALL_OUTSIDE_SCORE);
    Assert.assertEquals(
        all_outside_stat
            .getAggregateValues()
            .get(new Range(BigDecimal.valueOf(-1.0), BigDecimal.valueOf(-0.8)))
            .intValue(),
        0);
    Assert.assertEquals(
        all_outside_stat
            .getAggregateValues()
            .get(new Range(BigDecimal.valueOf(0.8), BigDecimal.valueOf(1.0)))
            .intValue(),
        0);
  }

  @Test
  public void testAvgOneOutsiderScore() {
    Assert.assertEquals(ONE_OUSIDE_STAT.getAverageScore(), -0.5, 0);
  }

  @Test
  public void testSumFrequency() {
    Assert.assertEquals(6, NORMAL_STAT.getAggregateValues().values().stream().mapToInt(i -> i.intValue()).sum());
    Assert.assertEquals(4, SYMMETRIC_STAT.getAggregateValues().values().stream().mapToInt(i -> i.intValue()).sum());
    Assert.assertEquals(3, EDGE_STAT.getAggregateValues().values().stream().mapToInt(i -> i.intValue()).sum());
    Assert.assertEquals(2, ONE_OUSIDE_STAT.getAggregateValues().values().stream().mapToInt(i -> i.intValue()).sum());
  }
}