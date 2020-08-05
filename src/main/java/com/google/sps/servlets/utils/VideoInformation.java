package com.google.sps.servlets.utils;

import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.google.common.collect.Iterables;

/**
 * VideoInformation contains detailed information of a Youtube Video's title, channel, number of
 * likes/dislikes and published date. It is used for users to retrieve the key information of a
 * specific video.
 */
public class VideoInformation {
  String videoName;
  String videoAuthor;
  int numLikes;
  int numDislikes;
  String publishDateString;

  public VideoInformation(VideoListResponse videoResponse) {
    Video video = Iterables.getOnlyElement(videoResponse.getItems());
    VideoSnippet videoSnippet = video.getSnippet();
    VideoStatistics videoStats = video.getStatistics();
    this.videoName = videoSnippet.getTitle();
    this.videoAuthor = videoSnippet.getChannelTitle();
    // Removes the unreadable code at the end of the dateString; Convert to yy-mm-dd format
    this.publishDateString = videoSnippet.getPublishedAt().toStringRfc3339().substring(0, 10);
    this.numLikes = videoStats.getLikeCount().intValue();
    this.numDislikes = videoStats.getDislikeCount().intValue();
  }
}
