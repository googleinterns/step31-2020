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
  // TODO: set the current Top n comment as 1; will be updated once we have the input from front-end
  private static final BigDecimal SCORE_INTERVAL = BigDecimal.valueOf(SCORE_INTERVAL_VAL);
  private static final BigDecimal UPPER_SCORE = BigDecimal.valueOf(UPPER_SCORE_VAL);
  private static final BigDecimal LOWER_SCORE = BigDecimal.valueOf(LOWER_SCORE_VAL);
  private static final Comparator<UserComment> ascendingScoreCompare =
      (UserComment o1, UserComment o2) -> Double.compare(o1.getScore(), o2.getScore());

  // Contains sentiment bucket information for all intervals
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
   * Constructor of Statistics to get average score and magnitude and create aggregate sentiment
   * bucket list to store each interval's information
   *
   * @param userCommentList given list of userComment objects
   * @param topNComments the number of highest magnitudes to retrieve
   */
  public Statistics(List<UserComment> userCommentList, int topNComments) {
    sentimentBucketList =
        categorizeToBucketList(
            userCommentList, LOWER_SCORE, UPPER_SCORE, SCORE_INTERVAL, topNComments);
    averageScore = getAverageScore(userCommentList);
    averageMagnitude = getAverageMagnitude(userCommentList);
  }

  /**
   * Categorize all score values into different range intervals and count the frequency for each
   * interval, and set the aggregatedValues.
   *
   * @param userCommentList a list of userComment analyzed from sentiment analysis with upadted
   *     score and magnitude
   * @param lowerEnd lower end boundary of the map
   * @param upperEnd upper end boundary of the map
   * @param interval interval for the range
   * @return a categorized map based on userCommentList from lowerEnd to upperEnd with interval
   */
  private List<SentimentBucket> categorizeToBucketList(
      List<UserComment> userCommentList,
      BigDecimal lowerEnd,
      BigDecimal upperEnd,
      BigDecimal interval,
      int topNumComments) {
    List<SentimentBucket> sentimentBucketList = new ArrayList<>();
    // Add score's interval to different ranges two sorting with and two pointers pop-up
    userCommentList.sort(ascendingScoreCompare);
    int updatingScoreIdx = 0;
    BigDecimal tempPoint;
    for (tempPoint = lowerEnd;
        tempPoint.compareTo(upperEnd) < 0;
        tempPoint = tempPoint.add(interval)) {
      BigDecimal nextPoint = upperEnd.min(tempPoint.add(interval));
      Range currentRange = new Range(tempPoint, nextPoint);
      int currentFrequency = 0;
      PriorityQueue<UserComment> highMagnitudeComments = new PriorityQueue<>();
      // loop through sorted scores within currentRange from updated score pointer and update its
      // corresponding appearance frequency
      int scoreIdx;
      for (scoreIdx = updatingScoreIdx; scoreIdx < userCommentList.size(); scoreIdx++) {
        BigDecimal scorePoint = BigDecimal.valueOf(userCommentList.get(scoreIdx).getScore());
        if ((scorePoint.compareTo(nextPoint) < 0) || nextPoint.compareTo(upperEnd) == 0) {
          currentFrequency += 1;
          // TODO: add topNumComments to the priority queue highMagnitudeList
        } else {
          break;
        }
      }
      // update the score pointer
      updatingScoreIdx = scoreIdx;
      sentimentBucketList.add(
          new SentimentBucket(
              convertQueueToList(highMagnitudeComments), currentFrequency, currentRange));
    }
    return sentimentBucketList;
  }

  private ArrayList convertQueueToList(PriorityQueue<UserComment> inputQueue) {
    ArrayList<UserComment> returnList = new ArrayList<>();
    while (!inputQueue.isEmpty()) {
      returnList.add(inputQueue.poll());
    }
    return returnList;
  }

  /**
   * Set the average value of given sentiment values.
   *
   * @param userCommentList a list of userComment to calculate the average score for
   * @return the average score of userCommentList
   */
  private double getAverageScore(List<UserComment> userCommentList) {
    return userCommentList.stream()
        .mapToDouble(UserComment::getScore)
        .average()
        .orElseThrow(
            () ->
                new RuntimeException("Unable to calculate average score due to empty input list."));
  }

  /**
   * Set the average value of given sentiment magnitude.
   *
   * @param userCommentList a list of userComment to calculate the average magnitude for
   * @return the average magnitude of userCommentList
   */
  private double getAverageMagnitude(List<UserComment> userCommentList) {
    return userCommentList.stream()
        .mapToDouble(UserComment::getMagnitude)
        .average()
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Unable to calculate average magnitude due to empty input list."));
  }
}
