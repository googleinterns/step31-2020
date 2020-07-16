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
  private final String FIRST_TOKEN_COMMENT = "First";
  private final String SECOND_TOKEN_COMMENT = "Second";

  // Simulate a CommentThreadListResponse with exactly as many comments as expected
  public void setUpYouTubeMocks(int numExpectedComments) throws Exception {
    YouTube mockedYoutube = mock(YouTube.class, RETURNS_DEEP_STUBS);
    YouTube.CommentThreads.List mockedCommentThreadList =
        mock(YouTube.CommentThreads.List.class, RETURNS_DEEP_STUBS);
    boolean useNextPageToken = (numExpectedComments > MAX_COMMENTS_PER_TOKEN);
    // This is only enough for two calls, all these tests require
    // Return on the first call either 100 comments or the desired number.
    // If there's no next page token, return nothing on the second call,
    // Otherwise return the remainder of comments
    CommentThreadListResponse firstResponse =
        mockThreadListResponse(
            Math.min(numExpectedComments, MAX_COMMENTS_PER_TOKEN),
            (useNextPageToken) ? NEXT_PAGE_TOKEN : null,
            FIRST_TOKEN_COMMENT);
    if (useNextPageToken) {
      CommentThreadListResponse secondResponse =
          mockThreadListResponse(
              Math.min(numExpectedComments - MAX_COMMENTS_PER_TOKEN, MAX_COMMENTS_PER_TOKEN),
              NEXT_PAGE_TOKEN,
              SECOND_TOKEN_COMMENT);
      when(mockedCommentThreadList.execute()).thenReturn(firstResponse, secondResponse);
    } else {
      when(mockedCommentThreadList.execute()).thenReturn(firstResponse);
    }

    when(mockedYoutube.commentThreads().list(anyString())).thenReturn(mockedCommentThreadList);
    commentRetriever = new YouTubeCommentRetriever(mockedYoutube);
  }

  // Creates list of comments of desired length.
  // Page token is needed for iteration but its contents are irrelevant.
  private CommentThreadListResponse mockThreadListResponse(
      int desiredComments, String nextPageToken, String commentContent) {
    CommentThread mockThread = dummyCommentThread(commentContent);
    return new CommentThreadListResponse()
        .setItems(Collections.nCopies(desiredComments, mockThread))
        .setNextPageToken(nextPageToken);
  }

  private CommentThread dummyCommentThread(String comment) {
    // This has to be done because for some reason it's impossible to directly change a comment's
    // content
    CommentThread mockedCommentThread = mock(CommentThread.class, RETURNS_DEEP_STUBS);
    when(mockedCommentThread.getSnippet().getTopLevelComment().getSnippet().getTextDisplay())
        .thenReturn(comment);
    return mockedCommentThread;
  }

  // The simplest case: extract 100 comments from a video with more than 100 comments
  @Test
  public void testDefaultBehaviour() throws Exception {
    setUpYouTubeMocks(HUNDRED);
    List<CommentThread> comments = commentRetriever.retrieveComments(POPULAR_VIDEO_URL, HUNDRED);
    Assert.assertEquals(comments.size(), HUNDRED);
  }

  // Extract more than 100 comments from a video
  @Test
  public void testExcessHundredComments() throws Exception {
    setUpYouTubeMocks(EXCESS_HUNDRED);
    List<CommentThread> comments =
        commentRetriever.retrieveComments(POPULAR_VIDEO_URL, EXCESS_HUNDRED);
    Assert.assertEquals(comments.size(), EXCESS_HUNDRED);
  }

  // Only load the comments that are in a video, without crashing
  @Test
  public void doesNotAttemptRetrieveExcess() throws Exception {
    setUpYouTubeMocks(1);
    List<CommentThread> comments =
        commentRetriever.retrieveComments(UNPOPULAR_VIDEO_URL, EXCESS_HUNDRED);
    Assert.assertEquals(comments.size(), 1);
  }

  // Retrieve a specific amount of comments less than 100
  @Test
  public void retrievesSpecificNumComments() throws Exception {
    setUpYouTubeMocks(12);
    List<CommentThread> comments = commentRetriever.retrieveComments(POPULAR_VIDEO_URL, 12);
    Assert.assertEquals(comments.size(), 12);
  }

  // Retrieve a specific amount of comments more than 100
  @Test
  public void retrievesSpecificNumCommentsExcessHundred() throws Exception {
    setUpYouTubeMocks(120);
    List<CommentThread> comments = commentRetriever.retrieveComments(POPULAR_VIDEO_URL, 120);
    Assert.assertEquals(comments.size(), 120);
  }

  @Test
  public void retrievesCorrectCommentContent() throws Exception {
    setUpYouTubeMocks(200);
    List<CommentThread> comments = commentRetriever.retrieveComments(POPULAR_VIDEO_URL, 200);
    String firstCommentContent =
        comments.get(0).getSnippet().getTopLevelComment().getSnippet().getTextDisplay();
    String lastCommentContent =
        comments.get(150).getSnippet().getTopLevelComment().getSnippet().getTextDisplay();
    Assert.assertEquals(FIRST_TOKEN_COMMENT, firstCommentContent);
    Assert.assertEquals(SECOND_TOKEN_COMMENT, lastCommentContent);
  }
}
