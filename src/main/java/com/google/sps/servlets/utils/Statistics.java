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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics {
  // Contains sentiment scores in the range [-1, 1] with 0.2 intervals.
  private Map<Range, Integer> aggregateValues;
  private double averageScore;

  public Map<Range, Integer> getAggregateValues() {
    return aggregateValues;
  }

  public double getAverageScore() {
    return averageScore;
  }

  public Statistics(List<Double> sentimentScores) {
    setAggregateScores(sentimentScores);
    setAverageScore(sentimentScores);
  }

  /**
   * Categorizes all score values into different range intervals,
   * counts the frequency for each interval, and set the aggregatedValues.
   * @param sentimentScores a list of score values from -1.0 to 1.0
   */
  private void setAggregateScores(List<Double> sentimentScores) {
    aggregateValues = new HashMap<>();
    BigDecimal curPoint = BigDecimal.valueOf(-1.0);
    BigDecimal interval = BigDecimal.valueOf(0.2);

    // Initialize the HashMap with 0.2 intervals
    while (curPoint.doubleValue() < 1.0) {
      Range currentRange = new Range(curPoint.doubleValue(), curPoint.add(interval).doubleValue());
      curPoint = curPoint.add(interval);
      aggregateValues.put(currentRange, 0);
    }

    // Add score's interval to different ranges
    for (Double score: sentimentScores) {
      Range foundRange = getInterval(score);
      if (foundRange != null) {
        aggregateValues.put(foundRange, aggregateValues.get(foundRange) + 1);
      }
    }
  }

  /**
   * Set the average score of given sentiment scores.
   * @param sentimentScores a list of score values from -1.0 to 1.0
   */
  private void setAverageScore(List<Double> sentimentScores) {
    averageScore = sentimentScores.stream().mapToDouble(i -> i)
                       .filter(score -> (score >= -1.0 && score <= 1.0)).average().orElse(0);
  }

  /**
   * Compute the interval of a specific score between -1 to 1.
   * @param score given score to find its inclusive start and exclusive end
   * @return the range that contains calculated start and end point
   */
  private Range getInterval(double score) {

    // return null for edge cases
    if ((score < -1) || (score > 1)) {
      return null;
    }

    // Round to its lower and upper point
    BigDecimal scoreVal = BigDecimal.valueOf(score);
    BigDecimal inclusiveStart;
    if (score == 1) {
      inclusiveStart = BigDecimal.valueOf(0.8);
    } else {
      BigDecimal roundedScore = scoreVal.setScale(1, BigDecimal.ROUND_DOWN);
      inclusiveStart = roundedScore.remainder(BigDecimal.valueOf(0.2)).doubleValue() == 0
                           ? roundedScore
                           : (roundedScore.subtract(BigDecimal.valueOf(0.1)));
    }
    BigDecimal exclusiveEnd = inclusiveStart.add(BigDecimal.valueOf(0.2));
    return new Range(inclusiveStart.doubleValue(),exclusiveEnd.doubleValue());
  }
}