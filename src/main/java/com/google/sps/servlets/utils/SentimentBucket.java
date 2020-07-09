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

public class SentimentBucket {
  private UserComment highestUserComment;
  private Integer frequency;
  private Range intervalRange;

  public UserComment getHighestUserComment() {
    return highestUserComment;
  }

  public Range getIntervalRange() {
    return intervalRange;
  }

  public Integer getFrequency() {
    return frequency;
  }

  public SentimentBucket(UserComment highestUserComment, int frequency, Range intervalRange) {
    this.highestUserComment = highestUserComment;
    this.frequency = frequency;
    this.intervalRange = intervalRange;
  }

  @Override
  public String toString() {
    return "(highestUserComment: "
        + highestUserComment
        + " frequency: "
        + frequency
        + " intervalRange"
        + intervalRange
        + ")\n";
  }

  @Override
  public boolean equals(Object bucketObject) {
    if (bucketObject == this) {
      return true;
    }
    if (!(bucketObject instanceof SentimentBucket)) {
      return false;
    }
    SentimentBucket bucketToCompare = (SentimentBucket) bucketObject;
    return (highestUserComment == null
            || highestUserComment.equals(bucketToCompare.getHighestUserComment()))
        && frequency == bucketToCompare.getFrequency()
        && intervalRange.equals(getIntervalRange());
  }
}
