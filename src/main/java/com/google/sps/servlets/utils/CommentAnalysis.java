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

import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class encapsulates each element in json comment array into separate user comment object and
 * does sentiment analysis on each of them.
 */
public class CommentAnalysis {
  private LanguageServiceClient languageService;

  /**
   * Constructor to create and initialize language service for sentiment analysis
   *
   * @throws IOException error when the service cannot be started successfully
   */
  public CommentAnalysis() throws IOException {
    this.languageService = LanguageServiceClient.create();
  }

  /**
   * Constructor for mocked test to pass in mocked language client service.
   *
   * @param languageService language service that has been created
   */
  public CommentAnalysis(LanguageServiceClient languageService) {
    this.languageService = languageService;
  }

  /**
   * It computes an overall statistics object from the retrieved youtube comments.
   *
   * @return a Statistics object that contains required values to display
   */
  public Statistics computeOverallStats(CommentThreadListResponse youtubeResponse) {
    // Retrieve comment content from youtubeResponse and calculate sentiment for each comment
    List<Sentiment> sentimentList =
        youtubeResponse.getItems().stream()
            .map(UserComment::new)
            .map(
                this::calculateSentimentForComment
            )
            .collect(Collectors.toList());
    return new Statistics(sentimentList);
  }

  /**
   * Perform sentiment analysis from language service for a single usercomment
   * @param comment a comment object to retrieve the content
   * @return a Sentiment with sentiment scores & magnitude
   */
  private Sentiment calculateSentimentForComment(UserComment comment) {
    return languageService
               .analyzeSentiment(
                   Document.newBuilder()
                       .setContent(comment.getCommentMsg())
                       .setType(Document.Type.PLAIN_TEXT)
                       .build())
               .getDocumentSentiment();
  }
  

  public void closeLanguage() {
    languageService.close();
  }
}
