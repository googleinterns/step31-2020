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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Statistics {
  private static final double LOWER_END_VAL = -1.0;
  private static final double UPPER_END_VAL = 1.0;
  private static final BigDecimal INTERVAL = BigDecimal.valueOf(0.2);
  private static final BigDecimal UPPER_END = BigDecimal.valueOf(UPPER_END_VAL);
  private static final BigDecimal LOWER_END = BigDecimal.valueOf(LOWER_END_VAL);

  // Contains sentiment scores in the range [-1, 1] with given intervals.
  private Map<Range, Integer> aggregateValues;
  private double averageScore;

  public Map<Range, Integer> getAggregateValues() {
    return aggregateValues;
  }

  public double getAverageScore() {
    return averageScore;
  }

  /**
   * Constructor of Statistics to filter out invalid sentiment scores, set aggregate hash map and
   * average score
   *
   * @param sentimentScores given score values
   */
  public Statistics(List<Double> sentimentScores) throws RuntimeException {
    sentimentScores =
        sentimentScores.stream()
            .filter(score -> (score >= LOWER_END_VAL && score <= UPPER_END_VAL))
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
    aggregateValues = new HashMap<>();
    List<Range> keyRanges = new ArrayList<>();
    // Initialize the HashMap with intervals
    for (BigDecimal tempPoint = LOWER_END;
        tempPoint.compareTo(UPPER_END) < 0;
        tempPoint = tempPoint.add(INTERVAL)) {
      Range currentRange = new Range(tempPoint, UPPER_END.min(tempPoint.add(INTERVAL)));
      aggregateValues.put(currentRange, 0);
      keyRanges.add(currentRange);
    }
    // Add score's interval to different ranges two sorting with and two pointers pop-up
    sentimentScores.sort(Comparator.naturalOrder());
    int rangeIdx = 0;
    int scoreIdx = 0;
    while (rangeIdx < keyRanges.size()) {
      Range tempRange = keyRanges.get(rangeIdx);
      BigDecimal tempRangeLowerPoint = tempRange.getInclusiveStart();
      BigDecimal tempRangeUpperPoint = tempRange.getExclusiveEnd();
      while ((scoreIdx < sentimentScores.size())
          && (tempRangeLowerPoint.compareTo(BigDecimal.valueOf(sentimentScores.get(scoreIdx))) <= 0)
          && ((tempRangeUpperPoint.compareTo(BigDecimal.valueOf(sentimentScores.get(scoreIdx))) > 0)
              || (tempRangeUpperPoint.compareTo(UPPER_END) == 0))) {
        aggregateValues.put(tempRange, aggregateValues.get(tempRange) + 1);
        scoreIdx += 1;
      }
      rangeIdx += 1;
    }
  }

  /**
   * Set the average score of given sentiment scores. Throws an runtime exception if the average
   * score is not valid or none of the sentiment scores is valid.
   *
   * @param sentimentScores a list of score values from -1.0 to 1.0
   */
  private void setAverageScore(List<Double> sentimentScores) throws RuntimeException {
    averageScore =
        sentimentScores.stream()
            .mapToDouble(i -> i)
            .average()
            .orElseThrow(
                () ->
                    new RuntimeException(
                        "Unable to calculate sentiment average due to empty input list."));
  }
}
