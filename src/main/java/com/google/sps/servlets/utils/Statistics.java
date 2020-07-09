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

import com.google.appengine.api.users.User;
import com.google.cloud.language.v1.Sentiment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Statistics {
  private static final double LOWER_SCORE_VAL = -1.0;
  private static final double UPPER_SCORE_VAL = 1.0;
  private static final double SCORE_INTERVAL_VAL = 0.2;
  private static final BigDecimal SCORE_INTERVAL = BigDecimal.valueOf(SCORE_INTERVAL_VAL);
  private static final BigDecimal UPPER_SCORE = BigDecimal.valueOf(UPPER_SCORE_VAL);
  private static final BigDecimal LOWER_SCORE = BigDecimal.valueOf(LOWER_SCORE_VAL);
  private static final Comparator ascendingScoreCompare = new Comparator<UserComment>() {
    @Override
    public int compare(UserComment o1, UserComment o2) {
      return Double.compare(o1.getScore(),o2.getScore());
    }
  };
  
  // Contains sentiment bucket information for all intervals
  private List<SentimentBucket> sentimentBucketList;

  public List<SentimentBucket> getSentimentBucketList() {
    return sentimentBucketList;
  }

  private double averageScore;

  public double getAverageMagnitude() {
    return averageMagnitude;
  }

  private double averageMagnitude;
  

  public double getAverageScore() {
    return averageScore;
  }

  /**
   * Constructor of Statistics to get score and magnitude, filter out invalid data, set aggregate
   * hash map and average score and magnitude.
   *
   * @param userCommentList given list of userComment objects
   */
  public Statistics(List<UserComment> userCommentList) {
    sentimentBucketList =
        categorizeToAggregateMap(userCommentList, LOWER_SCORE, UPPER_SCORE, SCORE_INTERVAL);
    averageScore = getAverageScore(userCommentList);
    averageMagnitude = getAverageMagnitude(userCommentList);
  }

  /**
   * Categorize all score values into different range intervals and count the frequency for each
   * interval, and set the aggregatedValues.
   *
   * @param userCommentList a list of score values from lowerEnd to upperEnd
   * @param lowerEnd lower end boundary of the map
   * @param upperEnd upper end boundary of the map
   * @param interval interval for the range
   * @return a categorized map based on userCommentList from lowerEnd to upperEnd with interval
   */
  private List<SentimentBucket> categorizeToAggregateMap(
      List<UserComment> userCommentList,
      BigDecimal lowerEnd,
      BigDecimal upperEnd,
      BigDecimal interval) {
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
      UserComment highestUserComment = null;
      // loop through sorted scores within currentRange from updated score pointer and update its
      // corresponding appearance frequency in aggregatedValues
      int scoreIdx;
      for (scoreIdx = updatingScoreIdx; scoreIdx < userCommentList.size(); scoreIdx++) {
        BigDecimal scorePoint = BigDecimal.valueOf(userCommentList.get(scoreIdx).getScore());
        if ((scorePoint.compareTo(nextPoint) < 0) || nextPoint.compareTo(upperEnd) == 0) {
          currentFrequency += 1;
          highestUserComment = (highestUserComment == null)
                                   ? userCommentList.get(scoreIdx)
                                   : highestUserComment.findHigherMagnitude(userCommentList.get(scoreIdx));
        } else {
          break;
        }
      }
      // update the score pointer
      updatingScoreIdx = scoreIdx;
      sentimentBucketList.add(new SentimentBucket(highestUserComment, currentFrequency, currentRange));
    }
    return sentimentBucketList;
  }

  /**
   * Set the average value of given sentiment values.
   *
   * @param userCommentList a list of userComment to calculate the average score for
   * @return the average score of userCommentList
   */
  private double getAverageScore(List<UserComment> userCommentList) {
    return userCommentList.parallelStream()
        .mapToDouble(UserComment::getScore)
        .average()
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Unable to calculate sentiment average due to empty input list."));
  }

  /**
   * Set the average value of given sentiment values.
   *
   * @param userCommentList a list of userComment to calculate the average score for
   * @return the average score of userCommentList
   */
  private double getAverageMagnitude(List<UserComment> userCommentList) {
    return userCommentList.parallelStream()
               .mapToDouble(UserComment::getMagnitude)
               .average()
               .orElseThrow(
                   () ->
                       new RuntimeException(
                           "Unable to calculate sentiment average due to empty input list."));
  }
}
