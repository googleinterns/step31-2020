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
const YOUTUBE_API_KEY = "AIzaSyDYfjWcy1hEe0V7AyaYzgIQm_rT-9XbiGs";

async function doIt() {
  const response = await fetch('https://www.youtube.com/results?search_query=hello');
  const json = await response.json();
  console.log(JSON.stringify(json));  
  //url = getUrl();  
//   document.getElementById('video-results').innerHTML = "";
//   fetch(url).then(response => response.json()).then(data => {
//     videos = data.items;  
//     console.log(videos);
//     videos.forEach(function(video) {
//       document.getElementById('video-results').innerHTML += video.snippet.title + "<br>"
//       + "Channel: " + video.snippet.channelTitle + "<br>";
//     });
//   });
}

function getSearchTerm() {
  return document.getElementById('search-input').value;
}

function getUrl() {
  url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=5&q=" + getSearchTerm() + "&key=" + YOUTUBE_API_KEY;
  return url;
}
// https://www.googleapis.com/youtube/v3/search?part=snippet&order=relevance&maxResults=5&q=hello&type=video&key=AIzaSyDYfjWcy1hEe0V7AyaYzgIQm_rT-9XbiGs