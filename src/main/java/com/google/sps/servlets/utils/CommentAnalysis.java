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

import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * This class encapsulates the each element in json comment array into separate user comment object
 * and do sentiment analysis on each of them.
 */
public class CommentAnalysis {
  private CommentThreadListResponse youtuberesponse;
  private LanguageServiceClient languageService;

  public CommentAnalysis(CommentThreadListResponse youtuberesponse) throws IOException {
    this.youtuberesponse = youtuberesponse;
    this.languageService = LanguageServiceClient.create();
  }

  /**
   * It computes an overall statistics score from the retrieved youtube comments.
   * @return a Statistics object that contains required values to display
   */
  public Statistics computeOverallStats() {
    ArrayList<Double> scoreValues = new ArrayList<>();
    for (CommentThread commentThread: youtuberesponse.getItems()) {
      UserComment userComment = new UserComment(commentThread);
      scoreValues.add(this.sentiAnalysisScore(userComment));
    }
    double avgScore = scoreValues.stream().mapToDouble(i -> i).average().orElse(0);
    return new Statistics(this.categorizeInterval(scoreValues), avgScore);
  }

  /**
   * Perform sentiment analysis of comment.
   * @return sentiment score
   * @throws IOException if the sentiment analysis API is not working, throw the IOExeption
   */
  private double sentiAnalysisScore(UserComment comment) {
    // Start Sentiment Analysis Service.
    Document doc = Document.newBuilder().setContent(comment.getCommentMsg())
                       .setType(Document.Type.PLAIN_TEXT).build();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    double score = 0.0;
    if (sentiment != null) {
      score = (double) sentiment.getScore();
    }
    comment.setSentimentScore(score);
    return score;
  }

  /**
   * It categorizes all score values into different intervals of 0.2 from -1.0 to 1.0
   * and counts the frequency for each interval.
   * @param scoreValues a list of score values from -1.0 to 1.0
   * @return a list of 10 integers representing scoreValues' appearance in differnt intervals
   */
  public List<Integer> categorizeInterval(List<Double> scoreValues) {
    List<Integer> aggregatedValues = new ArrayList<>(Collections.nCopies(10, 0));
    for (Double score: scoreValues) {
      int index;
      if ((-1.0 <= score) && (score < 1.0)) {
        index = (int) Math.floor((score + 1) / 0.2);
      } else if (score == 1.0) {
        index = aggregatedValues.size() - 1;
      } else {
        continue;
      }
      aggregatedValues.set(index, aggregatedValues.get(index) + 1);
    }
    return aggregatedValues;
  }

  public void closeLangauge() {
    languageService.close();
  }
}
