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

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentSnippet;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadSnippet;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.sps.servlets.utils.CommentAnalysis;
import com.google.sps.servlets.utils.Range;
import com.google.sps.servlets.utils.SentimentBucket;
import com.google.sps.servlets.utils.Statistics;
import com.google.sps.servlets.utils.UserComment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
  private static final double SCORE_INTERVAL_VAL = 0.2;
  private static final BigDecimal SCORE_INTERVAL = BigDecimal.valueOf(SCORE_INTERVAL_VAL);
  private static final BigDecimal UPPER_SCORE = BigDecimal.valueOf(UPPER_SCORE_VAL);
  private static final BigDecimal LOWER_SCORE = BigDecimal.valueOf(LOWER_SCORE_VAL);

  private static final float TEST_SCORE = 0.23f;
  private static final float TEST_MAGNITUDE = 1.5f;

  /**
   * It constructs a List with sentiment bucket for expected score categorizations, frequency and
   * top comments for testing
   *
   * @param userCommentList a list of userComment corresponding to each interval that have top N
   *     magnitude
   * @param frequency a list of frequency corresponding to each interval sorted in ascending order
   * @return constructed hashmap with keys as ranges based on intervals and values corresponding to
   *     frequency input
   */
  private List<SentimentBucket> constructSentimentBucketListFromCommentList(
      List<List<UserComment>> userCommentList, List<Integer> frequency) {
    int listIndex = 0;
    List<SentimentBucket> expectedBucketList = new ArrayList<>();
    for (BigDecimal tempPoint = LOWER_SCORE;
        tempPoint.compareTo(UPPER_SCORE) < 0;
        tempPoint = tempPoint.add(SCORE_INTERVAL)) {
      BigDecimal nextPoint = UPPER_SCORE.min(tempPoint.add(SCORE_INTERVAL));
      Range currentRange = new Range(tempPoint, nextPoint);
<<<<<<< HEAD
      List<UserComment> curUserCommentList =
=======
      List<UserComment> curMagnitudeList =
>>>>>>> 524b5f4538171a272d02294b50fc0e9d67c4340c
          userCommentList.get(listIndex) == null
              ? new ArrayList<>()
              : userCommentList.get(listIndex);
      expectedBucketList.add(
<<<<<<< HEAD
          new SentimentBucket(curUserCommentList, frequency.get(listIndex), currentRange));
=======
          new SentimentBucket(curMagnitudeList, frequency.get(listIndex), currentRange));
>>>>>>> 524b5f4538171a272d02294b50fc0e9d67c4340c
      listIndex = listIndex + 1;
    }
    return expectedBucketList;
  }

  @Rule public ExpectedException exception = ExpectedException.none();

  @Test
  public void testCalculateSentiment() {
    // Simulate and test the process in comment analysis language service

    // Declarations of mocked variables and set the dependencies between constructed comments and
    // threads
    CommentSnippet topCommentSnippet = new CommentSnippet().setTextDisplay("Test Message");
    Comment testTopComment = new Comment().setSnippet(topCommentSnippet);
    CommentThreadSnippet testThreadSnippet =
        new CommentThreadSnippet().setTopLevelComment(testTopComment);
    CommentThread testCommentThread = new CommentThread().setSnippet(testThreadSnippet);
    List<CommentThread> testCommentThreadList =
        new ArrayList<>(Arrays.asList(testCommentThread, testCommentThread));

    // Mock the service variables
    LanguageServiceClient mockedlanguageService =
        mock(LanguageServiceClient.class, Mockito.RETURNS_DEEP_STUBS);
    Sentiment mockedSentiment = mock(Sentiment.class);
    when(mockedSentiment.getScore()).thenReturn(TEST_SCORE);
    when(mockedSentiment.getMagnitude()).thenReturn(TEST_MAGNITUDE);
    when(mockedlanguageService.analyzeSentiment(any(Document.class)).getDocumentSentiment())
        .thenReturn(mockedSentiment);
    CommentAnalysis commentAnalysis = new CommentAnalysis(mockedlanguageService);

    // Compute and test the sentiment bucket from mocked language service
<<<<<<< HEAD
    // TODO: Add the top magnitude comment message in list as we don't currently have it, which
    // means expectedUserComment is all empty.
=======
    Statistics testStat = commentAnalysis.computeOverallStats(testCommentThreadList);
    UserComment testUserComment =
        new UserComment(
            TEST_ID, TEST_MESSAGE, new DateTime(new Date()), TEST_SCORE, TEST_MAGNITUDE);
>>>>>>> 524b5f4538171a272d02294b50fc0e9d67c4340c
    List<List<UserComment>> expectedUserComment =
        new ArrayList<>(Arrays.asList(null, null, null, null, null, null, null, null, null, null));
    List<Integer> expectedFrequency = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 2, 0, 0, 0));
    Assert.assertNotNull(testStat);
    Assert.assertNotNull(testStat.getSentimentBucketList());
    Assert.assertEquals(TEST_SCORE, testStat.getAverageScore(), 0.01);
    Assert.assertEquals(TEST_MAGNITUDE, testStat.getAverageMagnitude(), 0.01);
    Assert.assertEquals(
        constructSentimentBucketListFromCommentList(expectedUserComment, expectedFrequency),
        testStat.getSentimentBucketList());
  }

  @Test
<<<<<<< HEAD
  public void testNormalScoreCases() {
    // cases: two user comments with sentiment score in the same interval
=======
  public void testTopOneMagnitude() {
    // Cases: two user comments with sentiment score in the same interval and only reuqire top 1
    // comment
    UserComment comment1 =
        new UserComment("001", "First Normal Comment", new DateTime(new Date()), 0.1, 0.4);
    UserComment comment2 =
        new UserComment("002", "Second Normal Comment", new DateTime(new Date()), 0.11, 0.5);

    List<UserComment> inputUserComment = new ArrayList<>(Arrays.asList(comment1, comment2));
    List<List<UserComment>> expectedUserComment =
        new ArrayList<>(
            Arrays.asList(
                null,
                null,
                null,
                null,
                null,
                new ArrayList<>(Arrays.asList(comment2)),
                null,
                null,
                null,
                null));
    List<Integer> expectedFrequency = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 2, 0, 0, 0, 0));
    Statistics highestStat = new Statistics(inputUserComment, 1);
    Assert.assertEquals(
        constructSentimentBucketListFromCommentList(expectedUserComment, expectedFrequency),
        highestStat.getSentimentBucketList());
  }

  @Test
  public void testMoreThanOneMagnitude() {
    // Cases: two user comments with sentiment score in the same interval and require more than 1
    // top comments
>>>>>>> 524b5f4538171a272d02294b50fc0e9d67c4340c
    UserComment comment1 =
        new UserComment("001", "First Normal Comment", new DateTime(new Date()), 0.1, 0.4);
    UserComment comment2 =
        new UserComment("002", "Second Normal Comment", new DateTime(new Date()), 0.11, 0.5);

    List<UserComment> inputUserComment = new ArrayList<>(Arrays.asList(comment1, comment2));
<<<<<<< HEAD
    // TODO: Add the top magnitude comment message in list as we don't currently have it, which
    // means expectedUserComment is all empty.
=======
>>>>>>> 524b5f4538171a272d02294b50fc0e9d67c4340c
    List<List<UserComment>> expectedUserComment =
        new ArrayList<>(Arrays.asList(null, null, null, null, null, null, null, null, null, null));
    List<Integer> expectedFrequency = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 2, 0, 0, 0, 0));
<<<<<<< HEAD
    Statistics normalStat = new Statistics(inputUserComment, 2);
    Assert.assertEquals(0.105, normalStat.getAverageScore(), 0.01);
=======
    Statistics twoHighestStat = new Statistics(inputUserComment, 2);
    Assert.assertEquals(0.105, twoHighestStat.getAverageScore(), 0.01);
    Assert.assertEquals(
        constructSentimentBucketListFromCommentList(expectedUserComment, expectedFrequency),
        twoHighestStat.getSentimentBucketList());
  }

  @Test
  public void testDistributeScore() {
    // Cases: two user comments with sentiment score on the two edge cases
    UserComment comment1 =
        new UserComment("003", "First Normal Comment", new DateTime(new Date()), -1.0, 0.4);
    UserComment comment2 =
        new UserComment("004", "Second Normal Comment", new DateTime(new Date()), 0.8, 0.5);

    List<UserComment> inputUserComment = new ArrayList<>(Arrays.asList(comment1, comment2));
    List<List<UserComment>> expectedUserComment =
        new ArrayList<>(
            Arrays.asList(
                new ArrayList<>(Arrays.asList(comment1)),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new ArrayList<>(Arrays.asList(comment2))));
    List<Integer> expectedFrequency = new ArrayList<>(Arrays.asList(1, 0, 0, 0, 0, 0, 0, 0, 0, 1));
    Statistics distStat = new Statistics(inputUserComment, 2);
>>>>>>> 524b5f4538171a272d02294b50fc0e9d67c4340c
    Assert.assertEquals(
        constructSentimentBucketListFromCommentList(expectedUserComment, expectedFrequency),
        normalStat.getSentimentBucketList());
  }
}
