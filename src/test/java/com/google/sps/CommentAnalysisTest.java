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

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
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
  private static final double LOWER_END_VAL = -1.0;
  private static final double UPPER_END_VAL = 1.0;
  private static final BigDecimal INTERVAL = BigDecimal.valueOf(0.2);
  private static final BigDecimal UPPER_END = BigDecimal.valueOf(UPPER_END_VAL);
  private static final BigDecimal LOWER_END = BigDecimal.valueOf(LOWER_END_VAL);
  private static final float TEST_SCORE = 0.23f;
  private static Comment testTopComment = new Comment();
  private static CommentSnippet topCommentSnippet = new CommentSnippet();
  private static CommentThread testCommentThread = new CommentThread();
  private static List<CommentThread> testCommentThreadList =
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
    youtubeResponse.setItems(testCommentThreadList);
  }

  private Map<Range, Integer> initializeMap(List<Integer> frequency) {
    if (frequency.size() != UPPER_END.subtract(LOWER_END).divide(INTERVAL, RoundingMode.UP).intValue()){
      throw new RuntimeException("Initialize list got wrong size");
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

  @Test
  public void testCalculateSentiment() {
    // This is a test method to calculate simulate and test the process in comment analysis language
    // service
    when(mockedlanguageService
            .analyzeSentiment(any(Document.class))
            .getDocumentSentiment()
            .getScore())
        .thenReturn(TEST_SCORE);
    Statistics testStat = commentAnalysis.computeOverallStats(youtubeResponse);
    Assert.assertNotNull(testStat);
    Assert.assertNotNull(testStat.getAggregateValues());
    Assert.assertEquals(testStat.getAverageScore(), 0.23, 0.01);
    Assert.assertEquals(initializeMap(new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 2, 0, 0, 0))), testStat.getAggregateValues());

  }

  @Test
  public void testNormalCases() {
    ArrayList<Double> scoreInRange =
        new ArrayList<>(Arrays.asList(0.001, 0.002, 0.003, 0.005, -0.1, -0.2));
    Statistics normalStat = new Statistics(scoreInRange);
    Assert.assertEquals(
        6, normalStat.getAggregateValues().values().stream().mapToInt(i -> i).sum());
    Assert.assertEquals(-0.048, normalStat.getAverageScore(), 0.01);
    Assert.assertEquals(initializeMap(new ArrayList<>(Arrays.asList(0, 0, 0, 0, 2, 4, 0, 0, 0, 0))), normalStat.getAggregateValues());
  }

  @Test
  public void testEdgeCases() {
    ArrayList<Double> edgeScore = new ArrayList<>(Arrays.asList(1.0, -1.0, 0.0));
    Statistics edgeStat = new Statistics(edgeScore);
    Assert.assertEquals(initializeMap(new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0, 1, 0, 0, 0, 1))), edgeStat.getAggregateValues());
  }

  @Test
  public void testOneOutsiderCases() {
    ArrayList<Double> oneOutsideScore = new ArrayList<>(Arrays.asList(-2.0, -1.0, 0.0));
    Statistics oneOutsideStat = new Statistics(oneOutsideScore);
    Assert.assertEquals(oneOutsideStat.getAverageScore(), -0.5, 0);
    Assert.assertEquals(initializeMap(new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0, 1, 0, 0, 0, 0))), oneOutsideStat.getAggregateValues());
  }

  @Test
  public void testExceptionAllOutsiderScore() {
    exception.expect(RuntimeException.class);
    ArrayList<Double> allOutsideScore = new ArrayList<>(Arrays.asList(-2.0, -3.0, 3.0, -100.2));
    Statistics allOutsideStat = new Statistics(allOutsideScore);
    Assert.assertEquals(
        allOutsideStat
            .getAggregateValues()
            .get(new Range(BigDecimal.valueOf(-1.0), BigDecimal.valueOf(-0.8)))
            .intValue(),
        0);
    Assert.assertEquals(
        allOutsideStat
            .getAggregateValues()
            .get(new Range(BigDecimal.valueOf(0.8), BigDecimal.valueOf(1.0)))
            .intValue(),
        0);
  }
}
