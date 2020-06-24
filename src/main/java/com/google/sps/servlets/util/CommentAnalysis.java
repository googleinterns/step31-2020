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

package com.google.sps.servlets.util;

import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import java.io.IOException;
import java.util.ArrayList;

// This class encapsulates the each element in json comment array into separate user comment object
// and do sentiment analysis on each of them
public class CommentAnalysis {
  CommentThreadListResponse youtuberesponse;
  private LanguageServiceClient languageService;

  public CommentAnalysis(CommentThreadListResponse youtuberesponse) throws IOException {
    this.youtuberesponse = youtuberesponse;
    this.languageService = LanguageServiceClient.create();
  }

  public Statistics computeOverallStats() {
    ArrayList<Double> aggregatedValues = new ArrayList<>();
    for (CommentThread commentThread: youtuberesponse.getItems()) {
      UserComment userComment = new UserComment(commentThread);
      try {
        aggregatedValues.add(this.sentiAnalysisScore(userComment));
      } catch (IOException e) {
        aggregatedValues.add(0.0);
      }
    }
    double avgScore = aggregatedValues.stream().mapToDouble(i -> i).average().orElse(0);
    return new Statistics(aggregatedValues, avgScore);
  }

  /**
   * Perform sentiment analysis of comment.
   * @return sentiment score
   * @throws IOException if the sentiment analysis API is not working, throw the IOExeption
   */
  public double sentiAnalysisScore(UserComment comment) throws IOException {

    // TODO: Since the sentiment analysis API is not working here,
    //  just add a check to see if succesfully receive the comments.
    System.out.println(comment.getCommentMsg());

    // Start Sentiment Analysis Service.
    // TODO: Test the language service client once deployed and merged with Front End
    Document doc = Document.newBuilder().setContent(comment.getCommentMsg())
                       .setType(Document.Type.PLAIN_TEXT).build();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    double score = (double) sentiment.getScore();
    comment.setSentimentScore(score);
    return score;
  }

  public void closeLangauge() {
    languageService.close();
  }
}
