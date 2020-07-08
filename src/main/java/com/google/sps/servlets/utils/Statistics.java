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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Statistics {
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

  // Contains sentiment scores and magnitudes with given intervals.
  private Map<Range, Integer> aggregateScores; // the score indicates the overall emotion of a document.
  private Map<Range, Integer> aggregateMagnitude; // the magnitude
  private double averageScore;

  public double getAverageMagnitude() {
    return averageMagnitude;
  }

  private double averageMagnitude;

  public Map<Range, Integer> getAggregateScores() {
    return aggregateScores;
  }

  public double getAverageScore() {
    return averageScore;
  }

  public Map<Range, Integer> getAggregateMagnitude() {
    return aggregateMagnitude;
  }

  /**
   * Constructor of Statistics to filter out invalid sentiment scores and magnitude, set aggregate
   * hash map and average score and magnitude.
   *
   * @param sentimentScores given score values
   * @param magnitudeScores given magnitude values
   */
  public Statistics(List<Double> sentimentScores, List<Double> magnitudeScores) {
    sentimentScores =
        sentimentScores.parallelStream()
            .filter(score -> (score >= LOWER_SCORE_VAL && score <= UPPER_SCORE_VAL))
            .collect(Collectors.toList());
    magnitudeScores =
        magnitudeScores.parallelStream()
            .filter(score -> (score >= LOWER_MAGNITUDE_VAL))
            .collect(Collectors.toList());
    aggregateScores =
        categorizeToAggregateMap(sentimentScores, LOWER_SCORE, UPPER_SCORE, SCORE_INTERVAL, false);
    aggregateMagnitude =
        categorizeToAggregateMap(
            magnitudeScores, LOWER_MAGNITUDE, UPPER_MAGNITUDE, MAGNITUDE_INTERVAL, true);
    averageScore = getAggregateAvg(sentimentScores);
    averageMagnitude = getAggregateAvg(magnitudeScores);
  }

  /**
   * Categorize all score values into different range intervals and count the frequency for each
   * interval, and set the aggregatedValues.
   *
   * @param sentimentValues a list of score values from lowerEnd to upperEnd
   * @param lowerEnd lower end boundary of the map
   * @param upperEnd upper end boundary of the map
   * @param interval interval for the range
   * @param overFlow True if the value is not bound by upperEnd; false otherwise
   * @return a categorized map based on sentimentValues from lowerEnd to upperEnd with interval
   */
  private Map<Range, Integer> categorizeToAggregateMap(
      List<Double> sentimentValues,
      BigDecimal lowerEnd,
      BigDecimal upperEnd,
      BigDecimal interval,
      Boolean overFlow) {
    Map<Range, Integer> aggregateValues = new HashMap<>();
    // Add score's interval to different ranges two sorting with and two pointers pop-up
    sentimentValues.sort(Comparator.naturalOrder());
    int updatingScoreIdx = 0;
    BigDecimal tempPoint;
    for (tempPoint = lowerEnd;
        tempPoint.compareTo(upperEnd) < 0;
        tempPoint = tempPoint.add(interval)) {
      BigDecimal nextPoint = upperEnd.min(tempPoint.add(interval));
      Range currentRange = new Range(tempPoint, nextPoint);
      aggregateValues.put(currentRange, 0);
      // loop through sorted scores within currentRange from updated score pointer and update its
      // corresponding appearance frequency in aggregatedValues
      int scoreIdx;
      for (scoreIdx = updatingScoreIdx; scoreIdx < sentimentValues.size(); scoreIdx++) {
        BigDecimal scorePoint = BigDecimal.valueOf(sentimentValues.get(scoreIdx));
        if ((scorePoint.compareTo(nextPoint) < 0) || nextPoint.compareTo(upperEnd) == 0) {
          aggregateValues.put(currentRange, aggregateValues.get(currentRange) + 1);
        } else {
          break;
        }
      }
      // update the score pointer
      updatingScoreIdx = scoreIdx;
    }

    // If the categorization allows overflow, add the value larger than upper end to the last
    // interval
    if (overFlow && updatingScoreIdx < sentimentValues.size()) {
      aggregateValues.put(
          new Range(tempPoint.subtract(interval), tempPoint),
          aggregateValues.get(new Range(tempPoint.subtract(interval), tempPoint))
              + (sentimentValues.size() - updatingScoreIdx));
    }
    return aggregateValues;
  }

  /**
   * Set the average value of given sentiment values.
   *
   * @param sentimentValues a list of values to calculate the average for
   * @return the average value of sentimentValues
   */
  private double getAggregateAvg(List<Double> sentimentValues) {
    return sentimentValues.parallelStream()
        .mapToDouble(i -> i)
        .average()
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Unable to calculate sentiment average due to empty input list."));
  }
}
