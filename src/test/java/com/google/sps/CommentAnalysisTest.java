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

import com.google.sps.servlets.utils.SentimentBucket;
import com.google.sps.servlets.utils.UserComment;
import java.util.AbstractList;
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
  private static final List<Double> MAGNITUDE_PLACEHOLDER =
      new ArrayList<>(Arrays.asList(0.1, 0.2, 0.3));
  private static final List<Double> SCORE_PLACEHOLDER =
      new ArrayList<>(Arrays.asList(0.1, -0.1, 0.2));

  CommentSnippet topCommentSnippet = new CommentSnippet().setTextDisplay("Test Message");
  Comment testTopComment = new Comment().setSnippet(topCommentSnippet);
  CommentThreadSnippet testThreadSnippet =
      new CommentThreadSnippet().setTopLevelComment(testTopComment);
  CommentThread testCommentThread = new CommentThread().setSnippet(testThreadSnippet);

  /**
   * It constructs a HashMap with current range for expected score categorizations for comparisions.
   *
   * @param userCommentList a list of userComment corresponding to each interval that have top N magnitude
   * @param frequency a list of frequency corresponding to each interval sorted in ascending order
   * @return constructed hashmap with keys as ranges based on intervals and values corresponding to
   *     frequency input
   */
  private List<SentimentBucket> constructRangeMapFromFrequencyList(
      List<List<UserComment>> userCommentList,List<Integer> frequency, BigDecimal lowerEnd, BigDecimal upperEnd, BigDecimal interval) {
    if (userCommentList.size() != frequency.size() || (frequency.size()
        != upperEnd.subtract(lowerEnd).divide(interval, 0, RoundingMode.UP).intValue())) {
      throw new RuntimeException("Initialize list in test function got wrong size");
    }
    int listPointer = 0;
    List<SentimentBucket> expectedBucketList = new ArrayList<>();
    for (BigDecimal tempPoint = lowerEnd;
        tempPoint.compareTo(upperEnd) < 0;
        tempPoint = tempPoint.add(interval)) {
      BigDecimal nextPoint = upperEnd.min(tempPoint.add(interval));
      Range currentRange = new Range(tempPoint, nextPoint);
      expectedBucketList.add(new SentimentBucket(userCommentList.get(listPointer),frequency.get(listPointer),currentRange));
      listPointer = listPointer + 1;
    }
    return expectedBucketList;
  }

  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testCalculateSentiment() {
    // This is a test method to calculate simulate and test the process in comment analysis language
    // service

    // Declarations of mocked variables and set the dependencies between constructed comments and
    // threads
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
    List<List<UserComment>> expectedUserComment = new ArrayList<>(Arrays.asList(null, null, null, null, null, null, null, null, null, null));
    List<Integer> expectedFrequency = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 2, 0, 0, 0));
    Assert.assertNotNull(testStat);
    Assert.assertNotNull(testStat.getSentimentBucketList());
    Assert.assertEquals( TEST_SCORE, testStat.getAverageScore(), 0.01);
    Assert.assertEquals(TEST_MAGNITUDE, testStat.getAverageMagnitude(), 0.01);
    Assert.assertEquals(
        constructRangeMapFromFrequencyList(
            expectedUserComment,
            expectedFrequency,
            LOWER_SCORE,
            UPPER_SCORE,
            SCORE_INTERVAL),
        testStat.getSentimentBucketList());  }

  @Test
  public void testNormalScoreCases() {
    UserComment comment1 = new UserComment(testCommentThread);
    comment1.setCommentId("001");
    comment1.setScore(0.1);
    comment1.setMagnitude(0.4);
    UserComment comment2 = new UserComment(testCommentThread);
    comment2.setCommentId("002");
    comment2.setScore(0.11);
    comment2.setMagnitude(0.5);

    List<UserComment> inputUserComment = new ArrayList<>(Arrays.asList(comment1, comment2));
    //TODO: currently we have not added the top magnitude comment message in list. The expectedUserComment is all empty.
    List<List<UserComment>> expectedUserComment = new ArrayList<>(Arrays.asList(null, null, null, null, null, null, null, null, null, null));
    List<Integer> expectedFrequency = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 2, 0, 0, 0, 0));
    Statistics normalStat = new Statistics(inputUserComment);
    Assert.assertEquals(0.105, normalStat.getAverageScore(), 0.01);
    Assert.assertEquals(
        constructRangeMapFromFrequencyList(
            expectedUserComment,
            expectedFrequency,
            LOWER_SCORE,
            UPPER_SCORE,
            SCORE_INTERVAL),
        normalStat.getSentimentBucketList());
  }
}
