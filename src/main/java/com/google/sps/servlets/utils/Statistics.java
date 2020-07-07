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
  private static final double LOWER_END_VAL = -1.0;
  private static final double UPPER_END_VAL = 1.0;
  private static final double ZERO_MAGNITUE_VAL = 0.0;
  private static final BigDecimal INTERVAL = BigDecimal.valueOf(0.2);
  private static final BigDecimal UPPER_END = BigDecimal.valueOf(UPPER_END_VAL);
  private static final BigDecimal LOWER_END = BigDecimal.valueOf(LOWER_END_VAL);
  private static final BigDecimal ZERO_MAGNITUDE = BigDecimal.valueOf(ZERO_MAGNITUE_VAL);

  // Contains sentiment scores in the range [-1, 1] with given intervals.
  private Map<Range, Integer> aggregateValues;
  private Map<Range, Integer> aggregateMagnitude;
  private double averageScore;

  public double getAverageMagnitude() {
    return averageMagnitude;
  }

  private double averageMagnitude;

  public Map<Range, Integer> getAggregateValues() {
    return aggregateValues;
  }

  public double getAverageScore() {
    return averageScore;
  }

  /**
   * Constructor of Statistics to filter out invalid sentiment scores, set aggregate hash map and
   * average score.
   *
   * @param sentimentScores given score values
   */
  public Statistics(List<Double> sentimentScores, List<Double> magnitudeScores)
      throws RuntimeException {
    sentimentScores =
        sentimentScores.stream()
            .filter(score -> (score >= LOWER_END_VAL && score <= UPPER_END_VAL))
            .collect(Collectors.toList());
    aggregateValues = categorizeToAggregateMap(sentimentScores, LOWER_END, UPPER_END, INTERVAL);
    aggregateMagnitude =
        categorizeToAggregateMap(
            magnitudeScores,
            BigDecimal.valueOf(0.0),
            BigDecimal.valueOf(4.0),
            BigDecimal.valueOf(0.3));
    averageScore = getAggregateAvg(sentimentScores);
    averageMagnitude = getAggregateAvg(sentimentScores);
  }

  /**
   * Categorize all score values into different range intervals and count the frequency for each
   * interval, and set the aggregatedValues.
   *
   * @param sentimentScores a list of score values from -1.0 to 1.0
   */
  private Map<Range, Integer> categorizeToAggregateMap(
      List<Double> sentimentScores, BigDecimal lowerEnd, BigDecimal upperEnd, BigDecimal interval) {
    Map<Range, Integer> aggregateValues = new HashMap<>();
    // Add score's interval to different ranges two sorting with and two pointers pop-up
    sentimentScores.sort(Comparator.naturalOrder());
    int updatingScoreIdx = 0;
    for (BigDecimal tempPoint = lowerEnd;
        tempPoint.compareTo(UPPER_END) < 0;
        tempPoint = tempPoint.add(interval)) {
      BigDecimal nextPoint = upperEnd.min(tempPoint.add(interval));
      Range currentRange = new Range(tempPoint, nextPoint);
      aggregateValues.put(currentRange, 0);
      // loop through sorted scores within currentRange from updated score pointer and update its
      // corresponding appearance frequency in aggregatedValues
      int scoreIdx;
      for (scoreIdx = updatingScoreIdx; scoreIdx < sentimentScores.size(); scoreIdx++) {
        BigDecimal scorePoint = BigDecimal.valueOf(sentimentScores.get(scoreIdx));
        if ((scorePoint.compareTo(nextPoint) < 0) || nextPoint.compareTo(upperEnd) == 0) {
          aggregateValues.put(currentRange, aggregateValues.get(currentRange) + 1);
        } else {
          break;
        }
      }
      // update the score pointer
      updatingScoreIdx = scoreIdx;
    }
    return aggregateValues;
  }

  /**
   * Set the average score of given sentiment scores. Throws an runtime exception if the average
   * score is not valid or none of the sentiment scores is valid.
   *
   * @param sentimentScores a list of score values from -1.0 to 1.0
   */
  private double getAggregateAvg(List<Double> sentimentScores) throws RuntimeException {
    double averageVal =
        sentimentScores.stream()
            .mapToDouble(i -> i)
            .average()
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Unable to calculate sentiment average due to empty input list."));
    return averageVal;
  }
}
