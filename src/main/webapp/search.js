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

const URL_STRUCTURE = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=5&q=";
const YOUTUBE_API_KEY = "AIzaSyDYfjWcy1hEe0V7AyaYzgIQm_rT-9XbiGs";

function getVideoResults() {
  // Clear the video results after a new search is made  
  const videoResults = document.getElementById("video-results");
  while (videoResults.firstChild) {
    myNode.removeChild(myNode.lastChild);
  }  

  url = getRequestUrl();  
  fetch(url).then(response => response.json()).then(data => {
    videos = data.items; 
    videos.forEach(function(video) {
      addVideoToResults(video);
    });
  });
}

function addVideoToResults(video) {
  youtubeUrl = "https://youtube.com/watch?v=" + video.id.videoId;
  thumbnail = video.snippet.thumbnails.default.url;
  description = video.snippet.title  + " - Channel: " + video.snippet.channelTitle;

  // Create button to select the video
  var button = document.createElement("INPUT");
  button.setAttribute("type", "button");
  button.setAttribute("onclick","getChart(youtubeUrl)");
  button.value = "SELECT";
  button.className = "button";

  // Add video description and thumbnail
  var label = document.createElement("p");
  var description = document.createTextNode(description);
  var image = document.createElement("IMG");
  image.src = thumbnail;

  // Append details to label and add to results div
  label.appendChild(image);
  label.appendChild(description);
  label.appendChild(button);

  var element = document.getElementById("video-results");
  element.appendChild(label); 
}

function getRequestUrl() {
  userSearchInput = document.getElementById('search-input').value;
  url = URL_STRUCTURE + getSearchTerm() + "&key=" + YOUTUBE_API_KEY;
  return url;
}
