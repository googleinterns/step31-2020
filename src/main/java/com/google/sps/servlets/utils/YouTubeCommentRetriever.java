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
  private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  // Limit of comments that can be obtained in one request
  private final Long COMMENT_LIMIT = 100L;
  // Parameters required by YouTube API to retrieve the comment threads
  private final String SNIPPET_PARAMETERS = "snippet,replies";
  private final String ORDER_PARAMETER = "relevance";

  private final String APPLICATION_NAME = "SAY";
  // TODO: have dev key come from centralized location, rather than being hard-coded.
  private String DEVELOPER_KEY = KeyRetriever.getApiKey();

  private YouTube youtubeService = null;

  public YouTubeCommentRetriever() throws Exception {
    youtubeService = getService();
  }

  public YouTubeCommentRetriever(YouTube youTube) {
    youtubeService = youTube;
  }

  public List<CommentThread> retrieveComments(String url, long maxComments) throws Exception {
    String nextPageToken = null;
    long numCommentsLeft = maxComments;
    long commentQueryLimit = 0;
    ArrayList allComments = new ArrayList<>();
    do {
      // If commentQueryLimit exceeds the number of comments on the video,
      // The API will simply return all the comments on a video.
      commentQueryLimit = Math.min(COMMENT_LIMIT, numCommentsLeft);
      numCommentsLeft -= COMMENT_LIMIT;
      CommentThreadListResponse commentResponse =
          generateYouTubeRequest(url, commentQueryLimit, nextPageToken);
      nextPageToken = commentResponse.getNextPageToken();
      allComments.addAll(commentResponse.getItems());
      // Continue retrieving comments until either reaching desired number or end of nextPageTokens.
    } while (nextPageToken != null && numCommentsLeft > 0);
    return allComments;
  }

  /**
   * @param url video id of the video to have its comments analyzed
   * @param maxResults is how many comments to be retrieved; Capped out at 100 per request but may
   *     be reduced for specific queries.
   * @return A list of comment threads to be aggregated to the overall list.
   */
  private CommentThreadListResponse generateYouTubeRequest(
      String url, long maxResults, String nextPageToken)
      throws GeneralSecurityException, IOException {
    YouTube.CommentThreads.List commentRequest =
        youtubeService.commentThreads().list(SNIPPET_PARAMETERS);
    commentRequest
        .setKey(DEVELOPER_KEY)
        .setVideoId(url)
        .setOrder(ORDER_PARAMETER)
        .setMaxResults(maxResults)
        .setPageToken(nextPageToken);
    return commentRequest.execute();
  }

  /**
   * Build and return an authorized API client service.
   *
   * @return an authorized API client service
   * @throws GeneralSecurityException, IOException
   */
  public YouTube getService() throws GeneralSecurityException, IOException {
    final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }
}
