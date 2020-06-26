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

import com.google.sps.servlets.Range;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics {
  // Aggregated score for a list of comments
  private Map<Range, Integer> aggregateValues;
  private double averageScore;

  public Statistics(List<Double> sentimentScores) {
    setAggregateScores(sentimentScores);
    setAverageScore(sentimentScores);
  }

  private void setAggregateScores(List<Double> sentimentScores) {
    // TODO(Xin): Add sorting code
    aggregateValues = new HashMap<>();
    aggregateValues.put(new Range(-1.0, 1), 1);
  }

  private void setAverageScore(List<Double> sentimentScores) {
    // TODO(Xin): Add average score method
    averageScore = 0;
  }
} 