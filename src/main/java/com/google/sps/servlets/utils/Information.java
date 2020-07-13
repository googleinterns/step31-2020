package com.google.sps.servlets.utils;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;

public class Information {
  String videoName;
  String videoAuthor;
  int numLikes;
  int numDislikes;
  String publishDate;
  public Information(VideoListResponse videoResponse) {
    Video video = videoResponse.getItems().get(0);
    VideoSnippet videoSnippet = video.getSnippet();
    VideoStatistics videoStats = video.getStatistics();
    this.videoName = videoSnippet.getTitle();
    this.videoAuthor =videoSnippet.getChannelTitle();
    this.publishDate = videoSnippet.getPublishedAt().toStringRfc3339();
    this.numLikes = videoStats.getLikeCount().intValue();
    this.numDislikes = videoStats.getDislikeCount().intValue();
  }
}
