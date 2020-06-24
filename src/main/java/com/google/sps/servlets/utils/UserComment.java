// Copyright 2019 Google LLC
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

public class UserComment {
  private String commentId;
  private String commentMsg;
  private DateTime publishDate;
  private Double sentimentScore;



  UserComment(CommentThread commentThread) {
    Comment topLevelComment =  commentThread.getSnippet().getTopLevelComment();
    this.commentId = topLevelComment.getId();
    this.commentMsg = topLevelComment.getSnippet().getTextDisplay();
    this.publishDate = topLevelComment.getSnippet().getPublishedAt();
  }

  public String getCommentId() {
    return commentId;
  }

  public void setCommentId(String commentId) {
    this.commentId = commentId;
  }

  public String getCommentMsg() {
    return commentMsg;
  }

  public void setCommentMsg(String commentMsg) {
    this.commentMsg = commentMsg;
  }

  public DateTime getPublishDate() {
    return publishDate;
  }

  public void setPublishDate(DateTime publishDate) {
    this.publishDate = publishDate;
  }

  public Double getSentimentScore() {
    return sentimentScore;
  }

  public void setSentimentScore(Double sentimentScore) {
    this.sentimentScore = sentimentScore;
  }

}
