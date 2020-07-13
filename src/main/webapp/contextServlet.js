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

async function getVideoContext() { 
  const urlInput = document.getElementById('link-input');
  const url = cleanseUrl(urlInput.value);
  const response = await fetch("/VideoContext?url="+url);
  const context = await response.json();
  return context;
}

/*
 * Extracts video id from full url
 */ 
function cleanseUrl(url) {
  // Split web address from parameters, extract first parameter
  // TODO: Add checks to make this work if video is not first parameter.
  var videoId = url.split("?");
  videoId = (videoId.length > 1) ? videoId[1].split("&")[0] : videoId[0];

  // If param name present, remove it to isolate video Id.
  videoId = videoId.replace("v=", "");
  
  return videoId;
}

/**
 * Fetches data and adds to html
 */
async function getContext() {
  $('form').submit(async function() {  
    videoContext = await getVideoContext();
    videoName = videoContext.name;
    videoAuthor = videoContext.author; 
    videoDate = videoContext.date;
    videoLikes = videoContext.numLikes;
    videoDislikes = videoContext.numDislikes;

    videoInfo = videoContext.name + "<br>Channel: " + videoAuthor + "</br>Date Published: " + videoDate.asString
        + "</br> Likes/dislikes" + videoLikes + " / " + videoDislikes;

    document.getElementById('video-context').innerHTML = videoInfo;   
  });
}

