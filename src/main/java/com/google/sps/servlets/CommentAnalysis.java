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

package com.google.sps.servlets;

import com.google.api.services.youtube.model.CommentThread;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import java.io.IOException;

// This class encapsulates the each element in json comment array into separate user comment object
// and do sentiment analysis on each of them
public class CommentAnalysis {
  private UserComment comment;

  public CommentAnalysis(CommentThread originalComment) {
    comment = new UserComment(originalComment);
  }

  /**
   * Perform sentiment analysis of comment.
   * @return sentiment score
   * @throws IOException if the sentiment analysis API is not working, throw the IOExeption
   */
  public float sentiAnalysis() throws IOException {
    // TODO: Since the sentiment analysis API is not working here,
    //  just add a check to see if succesfully receive the comments.
    System.out.println(comment.getCommentMsg());

    // Start Sentiment Analysis Service.
    // TODO: Test the language service client once deployed and merged with Front End
    Document doc = Document.newBuilder().setContent(comment.getCommentMsg())
                       .setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    float score = sentiment.getScore();
    languageService.close();
    return score;
  }
}
