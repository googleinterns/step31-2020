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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.google.sps.servlets.utils.YouTubeCommentRetriever;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
// Using wildcard import since documentation is unclear which package
// "USES_DEEP_STUBS" is imported from

@RunWith(JUnit4.class)
public class CommentRetrievalTest {

  private final int HUNDRED = 100;
  private final int EXCESS_HUNDRED = 200;
  private final int MAX_COMMENTS_PER_TOKEN = 100;
  private YouTubeCommentRetriever commentRetriever;
  // These url's no longer do anything; rather, serve as parameter for the call
  private final String POPULAR_VIDEO_URL = "dQw4w9WgXcQ";
  private final String UNPOPULAR_VIDEO_URL = "cA-arJ0T6L4";
  private final String NEXT_PAGE_TOKEN = "Some Page Token";


  public void setUp(int numExpectedComments) throws Exception{
    setUp(numExpectedComments, null);
  }
  // Simulate a CommentThreadListResponse with exactly as many comments as expected
  public void setUp(int numExpectedComments, String nextPageToken) throws Exception {
    YouTube mockedYoutube = mock(YouTube.class, RETURNS_DEEP_STUBS);
    YouTube.CommentThreads.List mockedCommentThreadList =
        mock(YouTube.CommentThreads.List.class, RETURNS_DEEP_STUBS);
    // This is only enough for two calls, all these tests require
    // Return on the first call either 100 comments or the desired number.
    // If there's no next page token, return nothing on the second call, 
    // Otherwise return the remainder of comments
    when(mockedCommentThreadList.execute())
        .thenReturn(
            mockThreadListResponse(Math.min(numExpectedComments, MAX_COMMENTS_PER_TOKEN), nextPageToken),
            mockThreadListResponse((nextPageToken == null) 
                ? 0
                : Math.min(numExpectedComments - MAX_COMMENTS_PER_TOKEN, MAX_COMMENTS_PER_TOKEN), nextPageToken));
    when(mockedYoutube.commentThreads().list(anyString()))
        .thenReturn(mockedCommentThreadList);
    commentRetriever = new YouTubeCommentRetriever(mockedYoutube);
  }

  // Creates list of comments of desired length. 
  // Page token is needed for iteration but its contents are irrelevant.
  private CommentThreadListResponse mockThreadListResponse(int desiredComments, String nextPageToken) {
    return new CommentThreadListResponse().setItems(
        Collections.nCopies(desiredComments, new CommentThread()))
            .setNextPageToken(nextPageToken);
  }

  // The simplest case: extract 100 comments from a video with more than 100 comments
  @Test
  public void testDefaultBehaviour() throws Exception {
    setUp(HUNDRED);
    List<CommentThread> comments = commentRetriever.retrieveComments(POPULAR_VIDEO_URL, HUNDRED);
    Assert.assertEquals(comments.size(), HUNDRED);
  }

  // Extract more than 100 comments from a video
  @Test
  public void testExcessHundredComments() throws Exception {
    setUp(EXCESS_HUNDRED, NEXT_PAGE_TOKEN);
    List<CommentThread> comments =
        commentRetriever.retrieveComments(POPULAR_VIDEO_URL, EXCESS_HUNDRED);
    Assert.assertEquals(comments.size(), EXCESS_HUNDRED);
  }

  // Only load the comments that are in a video, without crashing
  @Test
  public void doesNotAttemptRetrieveExcess() throws Exception {
    setUp(1);
    List<CommentThread> comments =
        commentRetriever.retrieveComments(UNPOPULAR_VIDEO_URL, EXCESS_HUNDRED);
    Assert.assertEquals(comments.size(), 1);
  }

  // Retrieve a specific amount of comments less than 100
  @Test
  public void retrievesSpecificNumComments() throws Exception {
    setUp(12);
    List<CommentThread> comments = commentRetriever.retrieveComments(POPULAR_VIDEO_URL, 12);
    Assert.assertEquals(comments.size(), 12);
  }

  // Retrieve a specific amount of comments more than 100
  @Test
  public void retrievesSpecificNumCommentsExcessHundred() throws Exception {
    setUp(120, NEXT_PAGE_TOKEN);
    List<CommentThread> comments = commentRetriever.retrieveComments(POPULAR_VIDEO_URL, 120);
    Assert.assertEquals(comments.size(), 120);
  }
}
