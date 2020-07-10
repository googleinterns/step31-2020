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

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Comment;
import com.google.api.services.youtube.model.CommentThread;
import com.google.cloud.language.v1.Sentiment;

public class UserComment {
  private String commentId;
  private String commentMsg;
  private DateTime publishDate;
  private double magnitude;
  private double score;

  /**
   * Encapsulate a comment Thread into an User Comment object.
   *
   * @param commentThread given comment thread retrieved from Youtube API
   */
  public UserComment(CommentThread commentThread) {
    Comment topLevelComment = commentThread.getSnippet().getTopLevelComment();
    this.commentId = topLevelComment.getId();
    this.commentMsg = topLevelComment.getSnippet().getTextDisplay();
    this.publishDate = topLevelComment.getSnippet().getPublishedAt();
  }

  /**
   * This is a easy way to construct a new userComment object for testing purpose.
   */
  public UserComment(String commentId, String commentMsg, DateTime publishDate, double score, double magnitude) {
    this.commentId = commentId;
    this.commentMsg = commentMsg;
    this.publishDate = publishDate;
    this.magnitude = magnitude;
    this.score = score;
  }

  public double getMagnitude() {
    return magnitude;
  }

  public double getScore() {
    return score;
  }

  public void setSentiment(Sentiment sentiment) {
    setMagnitude(sentiment.getMagnitude());
    setScore(sentiment.getScore());
  }

  public UserComment findHigherMagnitude(UserComment commentHigherMagnitude) {
    return magnitude <= commentHigherMagnitude.magnitude ? commentHigherMagnitude : this;
  }

  public String getCommentMsg() {
    return commentMsg;
  }

  public void setMagnitude(double magnitude) {
    this.magnitude = magnitude;
  }

  public void setScore(double score) {
    this.score = score;
  }

  public DateTime getPublishDate() {
    return publishDate;
  }

  public void setCommentId(String commentId) {
    this.commentId = commentId;
  }

  public String toString() {
    return commentMsg;
  }

  @Override
  public boolean equals(Object commentObject) {
    if (commentObject == this) {
      return true;
    }
    if (!(commentObject instanceof UserComment)) {
      return false;
    }
    UserComment commentToCompare = (UserComment) commentObject;
    return commentId == commentToCompare.commentId;
  }
}
