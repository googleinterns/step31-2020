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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Statistics {
  private static final double LOWER_SCORE_VAL = -1.0;
  private static final double UPPER_SCORE_VAL = 1.0;
  private static final double SCORE_INTERVAL_VAL = 0.2;
  private static final BigDecimal SCORE_INTERVAL = BigDecimal.valueOf(SCORE_INTERVAL_VAL);
  private static final BigDecimal UPPER_SCORE = BigDecimal.valueOf(UPPER_SCORE_VAL);
  private static final BigDecimal LOWER_SCORE = BigDecimal.valueOf(LOWER_SCORE_VAL);
  private static final Comparator<UserComment> ascendingScoreComparator =
      (UserComment o1, UserComment o2) -> Double.compare(o1.getScore(), o2.getScore());

  // Contains sentiment bucket information for all SCORE_INTERVALs
  private List<SentimentBucket> sentimentBucketList;
  private Map<String, Integer> wordFrequencyMap;
  private double averageMagnitude;
  private double averageScore;

  public List<SentimentBucket> getSentimentBucketList() {
    return sentimentBucketList;
  }

  public double getAverageMagnitude() {
    return averageMagnitude;
  }

  public double getAverageScore() {
    return averageScore;
  }

  public Map<String, Integer> getWordFrequencyMap() {
    return wordFrequencyMap;
  }

  /**
   * Constructor of Statistics to get average score and magnitude and create aggregate sorted
   * sentiment bucket list based on SCORE_INTERVALs' ascending ranges.
   *
   * @param userCommentList given list of userComment objects
   * @param topNComments the number of highest magnitudes to retrieve
   */
  public Statistics(List<UserComment> userCommentList, int topNComments) {
    sentimentBucketList = categorizeToBucketList(userCommentList, topNComments);
    averageScore = getAverageValue(userCommentList, "score");
    averageMagnitude = getAverageValue(userCommentList, "Magnitude");
    wordFrequencyMap = countWordFrequencyMap(userCommentList);
  }

  /**
   * Convert given userCommentList into a word map: {word: frequency}
   *
   * @param userCommentList a list of userComment with all fields updated
   * @return wordFrequencyMap to represent each word appearance time
   */
  private Map<String, Integer> countWordFrequencyMap(List<UserComment> userCommentList) {
    // Flatten all user comment message into a list of words
    Stream<String> allWordStream =
        userCommentList.stream()
            .map(comment -> comment.getCommentMsg().split("\\s+"))
            .map(wordArray -> new ArrayList<>(Arrays.asList(wordArray)))
            .flatMap(wordList -> wordList.stream());
    // Group and sum the appearances of each word
    Map<String, Integer> wordPairMap =
        allWordStream.collect(
            Collectors.groupingBy(word -> word, Collectors.summingInt(word -> 1)));
    return wordPairMap.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .limit(10)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Categorize all score values into different range SCORE_INTERVALs and count the frequency for
   * each SCORE_INTERVAL, and set the aggregatedValues.
   *
   * @param userCommentList a list of userComment analyzed from sentiment analysis with upadted
   *     score and magnitude
   * @return a categorized map based on userCommentList from LOWER_SCORE to UPPER_SCORE with
   *     SCORE_INTERVAL
   */
  private List<SentimentBucket> categorizeToBucketList(
      List<UserComment> userCommentList, int topNumComments) {
    List<SentimentBucket> sentimentBucketList = new ArrayList<>();
    // Add score's SCORE_INTERVAL to different ranges two sorting with and two pointers pop-up
    userCommentList.sort(ascendingScoreComparator);
    int updatingScoreIdx = 0;
    BigDecimal tempPoint;
    for (tempPoint = LOWER_SCORE;
        tempPoint.compareTo(UPPER_SCORE) < 0;
        tempPoint = tempPoint.add(SCORE_INTERVAL)) {
      BigDecimal nextPoint = UPPER_SCORE.min(tempPoint.add(SCORE_INTERVAL));
      Range currentRange = new Range(tempPoint, nextPoint);
      int currentFrequency = 0;
      PriorityQueue<UserComment> highMagnitudeComments = new PriorityQueue<>();
      // loop through sorted scores within currentRange from updated score pointer and update its
      // corresponding appearance frequency
      for (updatingScoreIdx = updatingScoreIdx;
          updatingScoreIdx < userCommentList.size();
          updatingScoreIdx++) {
        BigDecimal scorePoint =
            BigDecimal.valueOf(userCommentList.get(updatingScoreIdx).getScore());
        if ((scorePoint.compareTo(nextPoint) < 0) || nextPoint.compareTo(UPPER_SCORE) == 0) {
          currentFrequency += 1;
          // TODO: add topNumComments to the priority queue highMagnitudeList
        } else {
          break;
        }
      }
      sentimentBucketList.add(
          new SentimentBucket(
              convertQueueToList(highMagnitudeComments), currentFrequency, currentRange));
    }
    return sentimentBucketList;
  }

  /**
   * Covert the priority queue into an arrayList with preserved order
   *
   * @param inputQueue given priority queue of usercomments
   * @return ArrayList sorted based on inputQueue polling order
   */
  private ArrayList convertQueueToList(PriorityQueue<UserComment> inputQueue) {
    ArrayList<UserComment> returnList = new ArrayList<>();
    while (!inputQueue.isEmpty()) {
      returnList.add(inputQueue.poll());
    }
    return returnList;
  }

  /**
   * Set the average value of given sentiment magnitude.
   *
   * @param userCommentList a list of userComment to calculate the average value or magnitude for
   * @param scoreMagCheck parameter to set whether it it returns average score or magnitude
   * @return the average score or magnitude of userCommentList
   */
  private double getAverageValue(List<UserComment> userCommentList, String scoreMagCheck) {
    return userCommentList.stream()
        .mapToDouble(
            userComment ->
                scoreMagCheck == "score" ? userComment.getScore() : userComment.getMagnitude())
        .average()
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Unable to calculate average magnitude due to empty input list."));
  }
}
