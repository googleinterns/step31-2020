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
  const response = await fetch("/VideoContext?URL_PARAMETER="+url);
  const context = await response.json();
  return context;
}

/**
 * Fetches data and adds to html
 */
async function getContext() {
  $('form').submit(async function() {  
    videoContext = await getVideoContext();
    document.getElementById('video-context').innerHTML = videoInfoAsString(videoContext);   
  });
}

function videoInfoAsString(videoContext) { 
  return videoContext.videoName + "<br>Channel: " + videoContext.videoAuthor + 
    "<br>Date Published: " + videoContext.videoDate + "<br>Likes/dislikes" + 
    videoContext.videoLikes + " / " + videoContext.videoDislikes;
}