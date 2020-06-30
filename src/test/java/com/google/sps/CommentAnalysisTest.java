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

package com.google.sps;

import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.sps.servlets.utils.CommentAnalysis;
import com.google.sps.servlets.utils.Range;
import com.google.sps.servlets.utils.Statistics;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * This is a JUnit test for sentiment mockedAnalysis
 */
@RunWith(JUnit4.class)
public class CommentAnalysisTest {
  private static final CommentAnalysis mockedAnalysis = mock(CommentAnalysis.class);
  private static final CommentThreadListResponse mockedExpectedList = mock(CommentThreadListResponse.class);
  private static final CommentThreadListResponse mockedEdgeList = mock(CommentThreadListResponse.class);
  private static final ArrayList<Double> SCORE_IN_RANGE = new ArrayList<>(Arrays.asList(0.001, 0.002, 0.003, 0.005, -0.1, -0.2));
  private static final ArrayList<Double> EDGE_SCORE = new ArrayList<>(Arrays.asList(1.0, -1.0, 0.0));
  private static final ArrayList<Double> SYMMETRIC_SCORE = new ArrayList<>(Arrays.asList(0.5, 0.9,  -0.5, -0.9));
  private static final ArrayList<Double> ALL_OUSIDE_SCORE = new ArrayList<>(Arrays.asList(-2.0, -3.0, 3.0, -100.2));
  private static final ArrayList<Double> ONE_OUSIDE_SCORE = new ArrayList<>(Arrays.asList(-2.0, -1.0, 0.0));
  private static final Statistics NORMAL_STAT = new Statistics(SCORE_IN_RANGE);
  private static final Statistics EDGE_STAT = new Statistics(EDGE_SCORE);
  private static final Statistics SYMMETRIC_STAT = new Statistics(SYMMETRIC_SCORE);
  private static final Statistics ALL_OUSIDE_STAT = new Statistics(ALL_OUSIDE_SCORE);
  private static final Statistics ONE_OUSIDE_STAT = new Statistics(ONE_OUSIDE_SCORE);

  @Before
  public void setUp() {
    when(mockedAnalysis.computeOverallStats(mockedExpectedList)).thenReturn(NORMAL_STAT);
    when(mockedAnalysis.computeOverallStats(mockedEdgeList)).thenReturn(EDGE_STAT);
  }

  @Test
  public void testSentimentAnalysisInRange() {
    // Test the Sentiment Analysis Score within range -1 to 1.
    Statistics normalStat = mockedAnalysis.computeOverallStats(mockedExpectedList);
    Assert.assertTrue(Math.abs(normalStat.getAverageScore()) <= 1);
    // Test the Sentiment Analysis Score within range -1 to 1.
    Statistics edgeStat = mockedAnalysis.computeOverallStats(mockedExpectedList);
    Assert.assertTrue(Math.abs(edgeStat.getAverageScore()) <= 1);
  }

  @Test
  public void testCategorizationEdgeCases() {
    Assert.assertEquals(mockedAnalysis.computeOverallStats(mockedExpectedList).getAggregateValues().get(new Range(0,0.2)).intValue(), 4);
    Assert.assertEquals(NORMAL_STAT.getAggregateValues().get(new Range(-0.2,0)).intValue(), 2);
    Assert.assertEquals(EDGE_STAT.getAggregateValues().get(new Range(-1.0,-0.8)).intValue(), 1);
    Assert.assertEquals(EDGE_STAT.getAggregateValues().get(new Range(0.8,1)).intValue(), 1);
    Assert.assertEquals(ALL_OUSIDE_STAT.getAggregateValues().get(new Range(-1.0,-0.8)).intValue(), 0);
    Assert.assertEquals(ALL_OUSIDE_STAT.getAggregateValues().get(new Range(0.8,1.0)).intValue(), 0);
    Assert.assertEquals(ONE_OUSIDE_STAT.getAggregateValues().get(new Range(-1.0,-0.8)).intValue(), 1);
  }

  @Test
  public void testAvgScore() {
    Assert.assertEquals(EDGE_STAT.getAverageScore(), 0.0, 0);
    Assert.assertEquals(SYMMETRIC_STAT.getAverageScore(), 0.0, 0);
    Assert.assertEquals(ALL_OUSIDE_STAT.getAverageScore(), -99, 0);
    Assert.assertEquals(ONE_OUSIDE_STAT.getAverageScore(), -0.5, 0);
  }
}
