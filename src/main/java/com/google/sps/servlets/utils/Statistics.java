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
  private static final double INTERVAL = 0.2;
  private static final double UPPER_END = 1.0;
  private static final double LOWER_END = -1.0;
  // Contains sentiment scores in the range [-1, 1] with given intervals.
  private Map<Range, Integer> aggregateValues;
  private double averageScore;

  public Map<Range, Integer> getAggregateValues() {
    return aggregateValues;
  }

  public double getAverageScore() {
    return averageScore;
  }

  public Statistics(List<Double> sentimentScores) {
    sentimentScores =
        sentimentScores.stream()
            .filter(score -> (score >= LOWER_END && score <= UPPER_END))
            .collect(Collectors.toList());
    setAggregateScores(sentimentScores);
    setAverageScore(sentimentScores);
  }

  /**
   * Categorize all score values into different range intervals and count the frequency for each
   * interval, and set the aggregatedValues.
   *
   * @param sentimentScores a list of score values from -1.0 to 1.0
   */
  private void setAggregateScores(List<Double> sentimentScores) {
    // TODO: change Range class to BigDecimal to avoid imprecision conversions in floating points
    aggregateValues = new HashMap<>();
    BigDecimal interval = BigDecimal.valueOf(INTERVAL);
    BigDecimal upperPoint = BigDecimal.valueOf(UPPER_END);

    // Initialize the HashMap with intervals
    for (BigDecimal tempPoint = BigDecimal.valueOf(LOWER_END);
        tempPoint.compareTo(upperPoint) < 0;
        tempPoint = tempPoint.add(interval)) {
      Range currentRange =
          new Range(
              tempPoint.doubleValue(), Math.min(tempPoint.add(interval).doubleValue(), UPPER_END));
      aggregateValues.put(currentRange, 0);
    }
    // Add score's interval to different ranges two sorting with and two pointers pop-up
    sentimentScores.sort(Comparator.naturalOrder());
    BigDecimal curPoint = BigDecimal.valueOf(LOWER_END);
    int scoreIdx = 0;
    while ((scoreIdx < sentimentScores.size()) && (curPoint.compareTo(upperPoint) < 0)) {
      double scoreVal = sentimentScores.get(scoreIdx);
      // check available interval for scoreVal
      BigDecimal nextPoint = curPoint.add(interval);
      double intervalUpperVal = nextPoint.doubleValue();
      if (scoreVal < intervalUpperVal || intervalUpperVal == UPPER_END) {
        Range foundRange = new Range(curPoint.doubleValue(), intervalUpperVal);
        aggregateValues.put(foundRange, aggregateValues.get(foundRange) + 1);
        scoreIdx += 1;
      } else {
        curPoint = nextPoint;
      }
    }
  }

  /**
   * Set the average score of given sentiment scores. Returns -99 if the average score is not valid
   * or none of the sentiment scores is valid.
   *
   * @param sentimentScores a list of score values from -1.0 to 1.0
   */
  private void setAverageScore(List<Double> sentimentScores) {
    averageScore = sentimentScores.stream().mapToDouble(i -> i).average().orElse(-99);
  }
}
