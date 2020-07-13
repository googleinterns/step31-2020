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

import static org.mockito.Mockito.mock;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThread;
import com.google.sps.servlets.utils.YouTubeCommentRetriever;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class CommentRetrievalTest {

  private final int HUNDRED = 100;
  private final int EXCESS_HUNDRED = 200;

  // A video that has sufficient comments for any test (>900,000).
  private final String POPULAR_VIDEO_URL = "dQw4w9WgXcQ";
  // A video with very few comments (Unlikely to ever reach even 100)
  private final String UNPOPULAR_VIDEO_URL = "cA-arJ0T6L4";

  private YouTubeCommentRetriever commentRetriever;

  @Before
  public void testYoutubeGenerate() throws Exception {
    YouTube mockedYoutube = mock(YouTube.class);
    commentRetriever = new YouTubeCommentRetriever(mockedYoutube);//mock(YouTubeCommentRetriever.class);
    // Mockito.when(commentRetriever.getService()).thenReturn(mockedYoutube);
    
  }

  // The simplest case: extract 100 comments from a video with more than 100 comments
  @Test
  public void testDefaultBehaviour() throws Exception {
    List<CommentThread> comments = commentRetriever.retrieveComments(POPULAR_VIDEO_URL, HUNDRED);
    Assert.assertEquals(comments.size(), 100);
  }

  // Test that class properly consolidates a list of comments greater than the max allowed per
  // thread
  @Test
  public void testExcessHundredComments() throws Exception {
    List<CommentThread> comments =
        commentRetriever.retrieveComments(POPULAR_VIDEO_URL, EXCESS_HUNDRED);
    Assert.assertEquals(comments.size(), 200);
  }

  // Ensure that a crash does not occur when loading more comments than there are on a video
  @Test
  public void doesNotAttemptRetrieveExcess() throws Exception {
    List<CommentThread> comments =
        commentRetriever.retrieveComments(UNPOPULAR_VIDEO_URL, EXCESS_HUNDRED);
    // Assert uses < rather than == so that if additional comments are left it won't break the test.
    Assert.assertTrue(comments.size() < 200);
  }

  // Retrieve a specific amount of comments less than 100
  @Test
  public void retrievesSpecificNumComments() throws Exception {
    List<CommentThread> comments = commentRetriever.retrieveComments(POPULAR_VIDEO_URL, 12);
    Assert.assertEquals(comments.size(), 12);
  }

  // Retrieve a specific amount of comments more than 100
  @Test
  public void retrievesSpecificNumCommentsExcessHundred() throws Exception {
    List<CommentThread> comments = commentRetriever.retrieveComments(POPULAR_VIDEO_URL, 120);
    Assert.assertEquals(comments.size(), 120);
  }
}
