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

package com.google.sps.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Statistics {
  // Aggregated score for a list of comments  
  // Contains number of sentiment scores in the range [-1, 1] with 0.2 intervals.
  private List<Double> sentimentScores;
  private Map<Range, Integer> aggregateValues;
  private double averageScore;

  public Statistics(List<Double> sentimentScores) {
    this.sentimentScores = sentimentScores;
    getAggregateScores();
    getAverageScore();
  }

  private void getAggregateScores() {
    // TODO: Add sorting code (Xin)
    aggregateValues = new HashMap<>();
    map.put(new Range(-1.0, 1), 1);
  }

  private void getAverageScore() {
    // TODO: Add average score method (Xin)
    averageScore = 0;  
  }
} 