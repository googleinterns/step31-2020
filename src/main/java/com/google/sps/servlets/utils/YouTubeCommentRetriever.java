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

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/*
 * Class to retrieve YouTube comments from a designated URL with certain parameters
 */
public class YouTubeCommentRetriever {
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  // Limit of comments that can be obtained in one request
  private static final Long COMMENT_LIMIT = 100L;
  // Parameters required by YouTube API to retrieve the comment threads
  private static final String SNIPPET_PARAMETERS = "snippet,replies";
  private static final String ORDER_PARAMETER = "relevance";

  private static final String APPLICATION_NAME = "SAY";
  // TODO: have dev key come from centralized location, rather than being hard-coded.
  private static final String DEVELOPER_KEY = "AIzaSyDYfjWcy1hEe0V7AyaYzgIQm_rT-9XbiGs";

  private static YouTube youtubeService = null;

  public static List<CommentThread> retrieveComments(String url, int maxComments) throws Exception {
    String nextPageToken = "";
    int numCommentsLeft = maxComments;
    long commentQueryLimit = 0;
    ArrayList allComments = new ArrayList<>();
    do {
      commentQueryLimit = Math.min(COMMENT_LIMIT, numCommentsLeft);
      numCommentsLeft -= COMMENT_LIMIT;
      YouTube.CommentThreads.List commentRequest = generateYouTubeRequest(url, commentQueryLimit);
      if (nextPageToken != null && nextPageToken != "") {
        commentRequest.setPageToken(nextPageToken);
      }
      CommentThreadListResponse commentResponse = commentRequest.execute();
      nextPageToken = commentResponse.getNextPageToken();
      // Add comment threads to big list
      allComments.addAll(commentResponse.getItems());
    } while (nextPageToken != null && numCommentsLeft > 0);
    return allComments;
  }

  /**
   * @param url video id of the video to have its comments analyzed
   * @param maxResults is how many comments to be retrieved; Capped out at 100 per request but may
   *     be reduced for specific queries.
   * @return A list of comment threads to be aggregated to the overall list.
   */
  private static YouTube.CommentThreads.List generateYouTubeRequest(String url, long maxResults)
      throws GeneralSecurityException, IOException {
    if(youtubeService == null) {
     youtubeService = getService();
    }
    YouTube.CommentThreads.List commentRequest =
        youtubeService.commentThreads().list(SNIPPET_PARAMETERS);
    return commentRequest
        .setKey(DEVELOPER_KEY)
        .setVideoId(url)
        .setOrder(ORDER_PARAMETER)
        .setMaxResults(maxResults);
  }

  /**
   * Build and return an authorized API client service.
   *
   * @return an authorized API client service
   * @throws GeneralSecurityException, IOException
   */
  private static YouTube getService() throws GeneralSecurityException, IOException {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }
}
