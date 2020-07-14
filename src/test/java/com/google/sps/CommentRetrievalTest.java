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
import com.google.api.services.youtube.model.Comment;
import com.google.sps.servlets.utils.YouTubeCommentRetriever;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
// Using wildcard import since documentation is unclear which package
// "USES_DEEP_STUBS" is imported from

@RunWith(JUnit4.class)
public class CommentRetrievalTest {

  private final int HUNDRED = 100;
  private final int EXCESS_HUNDRED = 200;

  private YouTubeCommentRetriever commentRetriever;
  // These url's no longer do anything; rather, serve as parameter for the call
  private final String POPULAR_VIDEO_URL = "dQw4w9WgXcQ";
  private final String UNPOPULAR_VIDEO_URL = "cA-arJ0T6L4";

  // Simulate a CommentThreadListResponse with exactly as many comments as expected
  public void setUp(int expectedComments) throws Exception {
    YouTube mockedYoutube = mock(YouTube.class, RETURNS_DEEP_STUBS);
    YouTube.CommentThreads.List mockedCommentThreadList =
        mock(YouTube.CommentThreads.List.class, RETURNS_DEEP_STUBS);
    when(mockedCommentThreadList.execute())
        .thenReturn(new CommentThreadListResponse().setItems(
            Collections.nCopies(expectedComments, new CommentThread())));
    when(mockedYoutube.commentThreads().list(anyString()))
        .thenReturn(mockedCommentThreadList);
    commentRetriever = new YouTubeCommentRetriever(mockedYoutube);
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
    setUp(EXCESS_HUNDRED);
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
    setUp(120);
    List<CommentThread> comments = commentRetriever.retrieveComments(POPULAR_VIDEO_URL, 120);
    Assert.assertEquals(comments.size(), 120);
  }
}
