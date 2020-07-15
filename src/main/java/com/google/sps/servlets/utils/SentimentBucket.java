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

import java.util.List;

public class SentimentBucket {

  private List<UserComment> topNComments; // userComment with top N magnitude within this interval
  private int frequency; // number of comments whose sentiment score is in this interval
  private Range intervalRange;

  public List<UserComment> getTopNComments() {
    return topNComments;
  }

  public Range getIntervalRange() {
    return intervalRange;
  }

  public Integer getFrequency() {
    return frequency;
  }

  public SentimentBucket(List<UserComment> topNComments, int frequency, Range intervalRange) {
    this.topNComments = topNComments;
    this.frequency = frequency;
    this.intervalRange = intervalRange;
  }

  /**
   * This is currently for debugging purpose to show what each sentiment bucket returns
   *
   * @return debugging result for each sentiment bucket
   */
//  @Override
//  public String toString() {
//    return "(topNComments: "
//        + topNComments
//        + " frequency: "
//        + frequency
//        + " intervalRange"
//        + intervalRange
//        + ")\n";
//  }

  /**
   * This is for testing purpose: if two sentiment bucket have same frequency, range and
   * topNComments, we consider them as same sentiment bucket
   *
   * @param bucketObject sentiment bucket object to compare
   * @return true if two sentiment buckets are identical; false otherwise
   */
  @Override
  public boolean equals(Object bucketObject) {
    if (bucketObject == this) {
      return true;
    }
    if (!(bucketObject instanceof SentimentBucket)) {
      return false;
    }
    SentimentBucket bucketToCompare = (SentimentBucket) bucketObject;
    return (topNComments.equals(bucketToCompare.getTopNComments()))
        && frequency == bucketToCompare.getFrequency()
        && intervalRange.equals(getIntervalRange());
  }
}
