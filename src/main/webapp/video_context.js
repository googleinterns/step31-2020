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

google.charts.load('current', {'packages': ['corechart']});
google.setOnLoadCallback(updateUIWithVideoContext);

/**
 * Retrieve the context of a Youtube video
 * @return {Json} Json object of video context information
 */
async function getVideoContext() {
  const urlInput = document.getElementById('link-input');
  const url = extractYouTubeUrl(urlInput.value);
  const response = await fetch('/VideoContext?url=' + url);
  const context = await response.json();
  return context;
}

/**
 * Fetches data and adds to html
 */
async function updateUIWithVideoContext() {
  $('form').submit(async function() {
    videoContext = await getVideoContext();
    document.getElementById('video-context').innerHTML =
      videoInfoAsHTML(videoContext);
  });
}

/**
 * Convert a video context json object into html format
 * @param {Json} videoContext Json object of video context information
 * @return {String} html format of video context
 */
function videoInfoAsHTML(videoContext) {
  return '<ul class="list-group">' +
    '<li class="list-group-item">' + 'Video Name: ' +
    videoContext.videoName + '</li>' +
    '<li class="list-group-item">' + 'Channel: ' +
    videoContext.videoAuthor + '</li>' +
    '<li class="list-group-item">' + 'Date Published: ' +
    videoContext.videoDate + '</li>' +
    '<li class="list-group-item">' + 'Likes: ' +
    videoContext.videoLikes + '</li>' +
    '<li class="list-group-item">' + 'DisLikes: ' +
    videoContext.videoDislikes + '</li>' +
    '</ul>';
}
