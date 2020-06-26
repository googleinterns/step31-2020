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

/**
 * Range for the start and end point of sentiment scores.
 */
public class Range {
  private double inclusiveStart;
  private double exclusiveEnd;

  public Range(double inclusiveStart, double exclusiveEnd) {
    this.inclusiveStart = inclusiveStart;
    this.exclusiveEnd = exclusiveEnd;
  }

  private double getInclusiveStart() {
    return inclusiveStart;  
  }

  private double getExclusiveEnd() {
    return exclusiveEnd;  
  }

  public double getInterval() {
    return exclusiveEnd - inclusiveStart;
  }

  @Override
  public boolean equals(Object objectToCompare) {
    if (!(objectToCompare instanceof Range)) {
      return false;
    }
    Range rangeToCompare = (Range) objectToCompare;
    return ((rangeToCompare.getExclusiveEnd() == exclusiveEnd)
                && (rangeToCompare.getInclusiveStart() == inclusiveStart));
  }

  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }

  @Override
  public String toString() {
    return "(" + inclusiveStart + ", " + exclusiveEnd + ")";
  }
}