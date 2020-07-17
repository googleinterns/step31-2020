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

package com.google.sps.servlets.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Statistics {
  private static final double LOWER_SCORE_VAL = -1.0;
  private static final double UPPER_SCORE_VAL = 1.0;
  private static final double SCORE_INTERVAL_VAL = 0.2;
  private static final BigDecimal SCORE_INTERVAL = BigDecimal.valueOf(SCORE_INTERVAL_VAL);
  private static final BigDecimal UPPER_SCORE = BigDecimal.valueOf(UPPER_SCORE_VAL);
  private static final BigDecimal LOWER_SCORE = BigDecimal.valueOf(LOWER_SCORE_VAL);
  private static final Comparator<UserComment> ascendingScoreComparator =
      (UserComment o1, UserComment o2) -> Double.compare(o1.getScore(), o2.getScore());
  private static final Comparator<UserComment> descendingMagnitudeComparator =
      (UserComment o1, UserComment o2) -> Double.compare(o1.getMagnitude(), o2.getMagnitude());

  // Contains sentiment bucket information for all SCORE_INTERVALs
  private List<SentimentBucket> sentimentBucketList;
  private double averageMagnitude;
  private double averageScore;

  public List<SentimentBucket> getSentimentBucketList() {
    return sentimentBucketList;
  }

  public double getAverageMagnitude() {
    return averageMagnitude;
  }

  public double getAverageScore() {
    return averageScore;
  }

  /**
   * Constructor of Statistics to get average score and magnitude and create aggregate sorted
   * sentiment bucket list based on SCORE_INTERVALs' ascending ranges.
   *
   * @param userCommentList given list of userComment objects
   * @param topNComments the number of highest magnitudes to retrieve
   */
  public Statistics(List<UserComment> userCommentList, int topNComments) {
    sentimentBucketList = categorizeToBucketList(userCommentList, topNComments);
    averageScore = getAverageValue(userCommentList, "score");
    averageMagnitude = getAverageValue(userCommentList, "Magnitude");
  }

  /**
   * Categorize all score values into different range SCORE_INTERVALs and count the frequency for
   * each SCORE_INTERVAL, and set the aggregatedValues.
   *
   * @param userCommentList a list of userComment analyzed from sentiment analysis with upadted
   *     score and magnitude
   * @return a categorized map based on userCommentList from LOWER_SCORE to UPPER_SCORE with
   *     SCORE_INTERVAL
   */
  private List<SentimentBucket> categorizeToBucketList(
      List<UserComment> userCommentList, int topNumComments) {
    List<SentimentBucket> sentimentBucketList = new ArrayList<>();
    // Add score's SCORE_INTERVAL to different ranges two sorting with and two pointers pop-up
    userCommentList.sort(ascendingScoreComparator);
    int updatingScoreIdx = 0;
    BigDecimal tempPoint;
    for (tempPoint = LOWER_SCORE;
        tempPoint.compareTo(UPPER_SCORE) < 0;
        tempPoint = tempPoint.add(SCORE_INTERVAL)) {
      BigDecimal nextPoint = UPPER_SCORE.min(tempPoint.add(SCORE_INTERVAL));
      Range currentRange = new Range(tempPoint, nextPoint);
      int currentFrequency = 0;
      PriorityQueue<UserComment> descendingCommentMagnitudeQueue =
          new PriorityQueue<>(topNumComments, descendingMagnitudeComparator);
      // loop through sorted scores within currentRange from updated score pointer, update its
      // corresponding appearance frequency, and store the comments with topNumComments high
      // magnitude
      for (updatingScoreIdx = updatingScoreIdx;
          updatingScoreIdx < userCommentList.size();
          updatingScoreIdx++) {
        BigDecimal scorePoint =
            BigDecimal.valueOf(userCommentList.get(updatingScoreIdx).getScore());
        if ((scorePoint.compareTo(nextPoint) < 0) || nextPoint.compareTo(UPPER_SCORE) == 0) {
          currentFrequency += 1;
          addToFixedQueue(
              userCommentList.get(updatingScoreIdx),
              descendingCommentMagnitudeQueue,
              topNumComments);
        } else {
          break;
        }
      }
      sentimentBucketList.add(
          new SentimentBucket(
              convertQueueToDescendingList(descendingCommentMagnitudeQueue),
              currentFrequency,
              currentRange));
    }
    return sentimentBucketList;
  }

  /**
   * Convert a priority queue of userComments with high magnitude to a list of userComments with
   * descending magnitudes
   *
   * @param inputQueue fixed size priority queue that stores userComment based on descending order
   *     of magnitude
   * @return a list of userComment with descending magnitudes
   */
  private ArrayList<UserComment> convertQueueToDescendingList(
      PriorityQueue<UserComment> inputQueue) {
    ArrayList<UserComment> returnList = new ArrayList<>();
    while (!inputQueue.isEmpty()) {
      returnList.add(inputQueue.poll());
    }
    return returnList;
  }

  /**
   * Set the average value of given sentiment magnitude.
   *
   * @param userCommentList a list of userComment to calculate the average value or magnitude for
   * @param scoreMagCheck parameter to set whether it it returns average score or magnitude
   * @return the average score or magnitude of userCommentList
   */
  private double getAverageValue(List<UserComment> userCommentList, String scoreMagCheck) {
    return userCommentList.stream()
        .mapToDouble(
            userComment ->
                scoreMagCheck == "score" ? userComment.getScore() : userComment.getMagnitude())
        .average()
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Unable to calculate average magnitude due to empty input list."));
  }

  /**
   * Add a new comment to priority queue. If the priority queue has not been filled to maxQueueSize,
   * directly add the comment in; If the last element in priority queue has smaller magnitude,
   * replace that with new comment; If they have the same magnitude, replace the last element with
   * new incoming comment that has higher score.
   *
   * @param newComment userComment to add into currentQueue
   * @param currentQueue priority queue of userComment sorted based on descending order of magnitude
   * @param maxQueueSize maximum size of priority queue
   */
  private void addToFixedQueue(
      UserComment newComment, PriorityQueue<UserComment> currentQueue, int maxQueueSize) {
    UserComment commentToAdd = newComment;
    if (currentQueue.size() == maxQueueSize) {
      commentToAdd = currentQueue.poll();
      // Since userCommentList has been sorted, if newComment and commentToAdd have same magnitude, add newComment since it has higher score.
      if (newComment.getMagnitude() >= commentToAdd.getMagnitude()) {
        commentToAdd = newComment;
      }
    }
    currentQueue.add(commentToAdd);
  }
}
