package com.google.sps.servlets.utils;

import com.google.api.services.youtube.model.Video;
// Data retrieved from HTTP that can be parsed retrieve specific attributes
import com.google.api.services.youtube.model.VideoListResponse;
// VideoSnippet contains basic details about a video, such as its title, description, and category.
import com.google.api.services.youtube.model.VideoSnippet;
// VideoStatistics contains publicly viewable video statistics like a video's viewcount and ratings
import com.google.api.services.youtube.model.VideoStatistics;
import com.google.common.collect.Iterables;

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
    this.publishDateString = videoSnippet.getPublishedAt().toStringRfc3339();
    this.numLikes = videoStats.getLikeCount().intValue();
    this.numDislikes = videoStats.getDislikeCount().intValue();
  }
}
