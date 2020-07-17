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

google.charts.load('current', {'packages':['corechart']});
google.setOnLoadCallback(updateUIWithVideoContext)

function cleanseUrl(url) {
  // Split web address from parameters, extract first parameter
  // TODO: Add checks to make this work if video is not first parameter.
  var videoId = url.split("?");
  videoId = (videoId.length > 1) ? videoId[1].split("&")[0] : videoId[0];

  // If param name present, remove it to isolate video Id.
  videoId = videoId.replace("v=", "");

  return videoId;
}

async function getVideoContext() {
  console.log("call get video context");
  const urlInput = document.getElementById('link-input');
  const url = cleanseUrl(urlInput.value);
  const response = await fetch("/VideoContext?url="+url);
  const context = await response.json();
  return context;
}

/**
 * Fetches data and adds to html
 */
async function updateUIWithVideoContext() {
  $('form').submit(async function() {
     videoContext = await getVideoContext();
     document.getElementById('video-context').innerHTML = videoInfoAsString(videoContext);
  });
}

function videoInfoAsString(videoContext) { 
  return '<ul class="list-group">'+
  '<li class="list-group-item">'+ "Video Name: " + videoContext.videoName + '</li>' +
  '<li class="list-group-item">'+ "Channel: " + videoContext.videoAuthor + '</li>' +
  '<li class="list-group-item">'+ "Date Published: " + videoContext.videoDate + '</li>' +
  '<li class="list-group-item">'+ "Likes: " + videoContext.videoLikes + '</li>' +
  '<li class="list-group-item">'+ "DisLikes: " + videoContext.videoDislikes + '</li>' +
'</ul>';
}