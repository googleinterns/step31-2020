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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  private static final double LOWER_END_VAL = -1.0;
  private static final double UPPER_END_VAL = 1.0;
  private static final BigDecimal INTERVAL = BigDecimal.valueOf(0.2);
  private static final BigDecimal UPPER_END = BigDecimal.valueOf(UPPER_END_VAL);
  private static final BigDecimal LOWER_END = BigDecimal.valueOf(LOWER_END_VAL);
  private static final float TEST_SCORE = 0.23f;

  /**
   * It constructs a HashMap with current range for expected score categorizations for comparisions.
   *
   * @param frequency a list of frequency corresponding to each interval sorted in ascending order
   * @return constructed hashmap with keys as ranges based on intervals and values corresponding to
   *     frequency input
   */
  private Map<Range, Integer> initializeMap(List<Integer> frequency) {
    if (frequency.size()
        != UPPER_END.subtract(LOWER_END).divide(INTERVAL, RoundingMode.UP).intValue()) {
      throw new RuntimeException("Initialize list in test function got wrong size");
    }
    int freqIdx = 0;
    Map<Range, Integer> expectedMap = new HashMap<>();
    for (BigDecimal tempPoint = LOWER_END;
        tempPoint.compareTo(UPPER_END) < 0;
        tempPoint = tempPoint.add(INTERVAL)) {
      BigDecimal nextPoint = UPPER_END.min(tempPoint.add(INTERVAL));
      Range currentRange = new Range(tempPoint, nextPoint);
      expectedMap.put(currentRange, frequency.get(freqIdx));
      freqIdx += 1;
    }
    return expectedMap;
  }

  @Rule public ExpectedException exception = ExpectedException.none();

  @Before
  public void setup() {}

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
    when(mockedlanguageService
            .analyzeSentiment(any(Document.class))
            .getDocumentSentiment()
            .getScore())
        .thenReturn(TEST_SCORE);
    CommentAnalysis commentAnalysis = new CommentAnalysis(mockedlanguageService);

    // Compute and test the score from mocked language service
    Statistics testStat = commentAnalysis.computeOverallStats(youtubeResponse);
    Assert.assertNotNull(testStat);
    Assert.assertNotNull(testStat.getAggregateValues());
    Assert.assertEquals(testStat.getAverageScore(), TEST_SCORE, 0.01);
    Assert.assertEquals(
        initializeMap(new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 2, 0, 0, 0))),
        testStat.getAggregateValues());
  }

  @Test
  public void testNormalCases() {
    ArrayList<Double> scoreInRange =
        new ArrayList<>(Arrays.asList(0.001, 0.002, 0.003, 0.005, -0.1, -0.2));
    Statistics normalStat = new Statistics(scoreInRange);
    Assert.assertEquals(
        6, normalStat.getAggregateValues().values().stream().mapToInt(i -> i).sum());
    Assert.assertEquals(-0.048, normalStat.getAverageScore(), 0.01);
    Assert.assertEquals(
        initializeMap(new ArrayList<>(Arrays.asList(0, 0, 0, 0, 2, 4, 0, 0, 0, 0))),
        normalStat.getAggregateValues());
  }

  @Test
  public void testEdgeCases() {
    ArrayList<Double> edgeScore = new ArrayList<>(Arrays.asList(1.0, -1.0, 0.0));
    Statistics edgeStat = new Statistics(edgeScore);
    Assert.assertEquals(
        initializeMap(new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0, 1, 0, 0, 0, 1))),
        edgeStat.getAggregateValues());
  }

  @Test
  public void testOneOutsiderCases() {
    ArrayList<Double> oneOutsideScore = new ArrayList<>(Arrays.asList(-2.0, -1.0, 0.0));
    Statistics oneOutsideStat = new Statistics(oneOutsideScore);
    Assert.assertEquals(oneOutsideStat.getAverageScore(), -0.5, 0);
    Assert.assertEquals(
        initializeMap(new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0, 1, 0, 0, 0, 0))),
        oneOutsideStat.getAggregateValues());
  }

  @Test
  public void testExceptionAllOutsiderScore() {
    exception.expect(RuntimeException.class);
    ArrayList<Double> allOutsideScore = new ArrayList<>(Arrays.asList(-2.0, -3.0, 3.0, -100.2));
    Statistics allOutsideStat = new Statistics(allOutsideScore);
    Assert.assertEquals(
        initializeMap(new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0))),
        allOutsideStat.getAggregateValues());
    Assert.assertEquals(
        0.0,
        allOutsideStat.getAverageScore(), 0.1);
  }
}
