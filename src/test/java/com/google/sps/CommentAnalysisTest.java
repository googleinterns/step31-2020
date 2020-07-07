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
import com.google.cloud.language.v1.Sentiment;
import com.google.sps.servlets.utils.CommentAnalysis;
import com.google.sps.servlets.utils.Range;
import com.google.sps.servlets.utils.Statistics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** This is a JUnit test for sentiment mockedAnalysis */
@RunWith(JUnit4.class)
public class CommentAnalysisTest {
  private static final double LOWER_SCORE_VAL = -1.0;
  private static final double UPPER_SCORE_VAL = 1.0;
  private static final double LOWER_MAGNITUDE_VAL = 0.0;
  private static final double UPPER_MAGNITUDE_VAL = 2.0;
  private static final double SCORE_INTERVAL_VAL = 0.2;
  private static final double MAGNITUDE_INTERVAL_VAL = 0.3;
  private static final BigDecimal SCORE_INTERVAL = BigDecimal.valueOf(SCORE_INTERVAL_VAL);
  private static final BigDecimal UPPER_SCORE = BigDecimal.valueOf(UPPER_SCORE_VAL);
  private static final BigDecimal LOWER_SCORE = BigDecimal.valueOf(LOWER_SCORE_VAL);
  private static final BigDecimal MAGNITUDE_INTERVAL = BigDecimal.valueOf(MAGNITUDE_INTERVAL_VAL);
  private static final BigDecimal LOWER_MAGNITUDE = BigDecimal.valueOf(LOWER_MAGNITUDE_VAL);
  private static final BigDecimal UPPER_MAGNITUDE = BigDecimal.valueOf(UPPER_MAGNITUDE_VAL);

  private static final float TEST_SCORE = 0.23f;
  private static final float TEST_MAGNITUDE = 1.5f;
  private static final List<Double> MAGNITDE_PLACEHOLDER =
      new ArrayList<>(Arrays.asList(0.1, 0.2, 0.3));
  private static final List<Double> SCORE_PLACEHOLDER =
      new ArrayList<>(Arrays.asList(0.1, -0.1, 0.2));

  /**
   * It constructs a HashMap with current range for expected score categorizations for comparisions.
   *
   * @param frequency a list of frequency corresponding to each interval sorted in ascending order
   * @return constructed hashmap with keys as ranges based on intervals and values corresponding to
   *     frequency input
   */
  private Map<Range, Integer> constructRangeMapFromFrequencyList(
      List<Integer> frequency, BigDecimal lowerEnd, BigDecimal upperEnd, BigDecimal interval) {
    if (frequency.size()
        != upperEnd.subtract(lowerEnd).divide(interval, 0, RoundingMode.UP).intValue()) {
      throw new RuntimeException("Initialize list in test function got wrong size");
    }
    int freqIdx = 0;
    Map<Range, Integer> expectedMap = new HashMap<>();
    for (BigDecimal tempPoint = lowerEnd;
        tempPoint.compareTo(upperEnd) < 0;
        tempPoint = tempPoint.add(interval)) {
      BigDecimal nextPoint = upperEnd.min(tempPoint.add(interval));
      Range currentRange = new Range(tempPoint, nextPoint);
      expectedMap.put(currentRange, frequency.get(freqIdx));
      freqIdx += 1;
    }
    return expectedMap;
  }

  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testCalculateSentiment() {
    // This is a test method to calculate simulate and test the process in comment analysis language
    // service

    // Declarations of mocked variables and set the dependencies between constructed comments and
    // threads
    CommentSnippet topCommentSnippet = new CommentSnippet().setTextDisplay("Test Message");
    Comment testTopComment = new Comment().setSnippet(topCommentSnippet);
    CommentThreadSnippet testThreadSnippet =
        new CommentThreadSnippet().setTopLevelComment(testTopComment);
    CommentThread testCommentThread = new CommentThread().setSnippet(testThreadSnippet);
    List<CommentThread> testCommentThreadList =
        new ArrayList<>(Arrays.asList(testCommentThread, testCommentThread));
    CommentThreadListResponse youtubeResponse = new CommentThreadListResponse();
    youtubeResponse.setItems(testCommentThreadList);

    // Mock the service variables
    LanguageServiceClient mockedlanguageService =
        mock(LanguageServiceClient.class, Mockito.RETURNS_DEEP_STUBS);
    Sentiment mockedSentiment = mock(Sentiment.class);
    when(mockedSentiment.getScore()).thenReturn(TEST_SCORE);
    when(mockedSentiment.getMagnitude()).thenReturn(TEST_MAGNITUDE);
    when(mockedlanguageService.analyzeSentiment(any(Document.class)).getDocumentSentiment())
        .thenReturn(mockedSentiment);
    CommentAnalysis commentAnalysis = new CommentAnalysis(mockedlanguageService);

    // Compute and test the score from mocked language service
    Statistics testStat = commentAnalysis.computeOverallStats(youtubeResponse);
    Assert.assertNotNull(testStat);
    Assert.assertNotNull(testStat.getAggregateScores());
    Assert.assertEquals(testStat.getAverageScore(), TEST_SCORE, 0.01);
    Assert.assertNotNull(testStat.getAggregateMagnitude());
    Assert.assertEquals(testStat.getAverageMagnitude(), TEST_MAGNITUDE, 0.01);
    Assert.assertEquals(
        constructRangeMapFromFrequencyList(
            new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 2, 0, 0, 0)),
            LOWER_SCORE,
            UPPER_SCORE,
            SCORE_INTERVAL),
        testStat.getAggregateScores());
    Assert.assertEquals(
        constructRangeMapFromFrequencyList(
            new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 2, 0)),
            LOWER_MAGNITUDE,
            UPPER_MAGNITUDE,
            MAGNITUDE_INTERVAL),
        testStat.getAggregateMagnitude());
  }

  @Test
  public void testNormalScoreCases() {
    ArrayList<Double> scoreInRange =
        new ArrayList<>(Arrays.asList(0.001, 0.002, 0.003, 0.005, -0.1, -0.2));
    Statistics normalStat = new Statistics(scoreInRange, MAGNITDE_PLACEHOLDER);
    Assert.assertEquals(
        6, normalStat.getAggregateScores().values().stream().mapToInt(i -> i).sum());
    Assert.assertEquals(-0.048, normalStat.getAverageScore(), 0.01);
    Assert.assertEquals(
        constructRangeMapFromFrequencyList(
            new ArrayList<>(Arrays.asList(0, 0, 0, 0, 2, 4, 0, 0, 0, 0)),
            LOWER_SCORE,
            UPPER_SCORE,
            SCORE_INTERVAL),
        normalStat.getAggregateScores());
  }

  @Test
  public void testEdgeScoreCases() {
    ArrayList<Double> edgeScore = new ArrayList<>(Arrays.asList(1.0, -1.0, 0.0));
    Statistics edgeStat = new Statistics(edgeScore, MAGNITDE_PLACEHOLDER);
    Assert.assertEquals(
        constructRangeMapFromFrequencyList(
            new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0, 1, 0, 0, 0, 1)),
            LOWER_SCORE,
            UPPER_SCORE,
            SCORE_INTERVAL),
        edgeStat.getAggregateScores());
  }

  @Test
  public void testOneOutsiderScoreCases() {
    ArrayList<Double> oneOutsideScore = new ArrayList<>(Arrays.asList(-2.0, -1.0, 0.0));
    Statistics oneOutsideStat = new Statistics(oneOutsideScore, MAGNITDE_PLACEHOLDER);
    Assert.assertEquals(oneOutsideStat.getAverageScore(), -0.5, 0);
    Assert.assertEquals(
        constructRangeMapFromFrequencyList(
            new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0, 1, 0, 0, 0, 0)),
            LOWER_SCORE,
            UPPER_SCORE,
            SCORE_INTERVAL),
        oneOutsideStat.getAggregateScores());
  }

  @Test
  public void testExceptionAllOutsiderScoreCases() {
    exception.expect(RuntimeException.class);
    ArrayList<Double> allOutsideScore = new ArrayList<>(Arrays.asList(-2.0, -3.0, 3.0, -100.2));
    Statistics allOutsideStat = new Statistics(allOutsideScore, MAGNITDE_PLACEHOLDER);
    Assert.assertEquals(
        constructRangeMapFromFrequencyList(
            new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)),
            LOWER_SCORE,
            UPPER_SCORE,
            SCORE_INTERVAL),
        allOutsideStat.getAggregateScores());
    Assert.assertEquals(0.0, allOutsideStat.getAverageScore(), 0.1);
  }

  @Test
  public void testNormalMagnitudeCases() {
    ArrayList<Double> normalMagnitude =
        new ArrayList<>(Arrays.asList(0.0, 0.2, 0.8, 1.2, 1.8, 2.0, 3.0, 5.0));
    Statistics normalStat = new Statistics(SCORE_PLACEHOLDER, normalMagnitude);
    Assert.assertEquals(
        constructRangeMapFromFrequencyList(
            new ArrayList<>(Arrays.asList(2, 0, 1, 0, 1, 0, 4)),
            LOWER_MAGNITUDE,
            UPPER_MAGNITUDE,
            MAGNITUDE_INTERVAL),
        normalStat.getAggregateMagnitude());
    Assert.assertEquals(1.74, normalStat.getAverageMagnitude(), 0.1);
  }

  @Test
  public void testAllOutsiderMagnitudeCases() {
    exception.expect(RuntimeException.class);
    ArrayList<Double> allOutsidersMagnitude = new ArrayList<>(Arrays.asList(-0.1, -0.2, -3.0));
    Statistics allOutsiderStat = new Statistics(SCORE_PLACEHOLDER, allOutsidersMagnitude);
    Assert.assertEquals(
        constructRangeMapFromFrequencyList(
            new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0)),
            LOWER_MAGNITUDE,
            UPPER_MAGNITUDE,
            MAGNITUDE_INTERVAL),
        allOutsiderStat.getAggregateMagnitude());
    Assert.assertEquals(0.0, allOutsiderStat.getAverageScore(), 0.1);
  }
}
