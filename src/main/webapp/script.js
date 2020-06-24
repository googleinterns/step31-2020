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

function getYouTubeComments() { 
  const urlInput = document.getElementById('url-entry');
  var url = cleanseUrl(urlInput.value);
  fetch("/YouTubeComments?url="+url)
      .then(response => response.json()).then((comments) =>{
        return comments;
    });
}

// Extracts video id from full url
function cleanseUrl(url) {
  // Split web address from parameters, extract first parameter
  var videoId = url.split("?");
  videoId = (videoId.length > 1) ? videoId[1].split("&")[0] : videoId[0];

  // Remove parameter name to isolate video Id.
  videoId = videoId.replace("v=", "");
  
  return videoId;
}