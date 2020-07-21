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
var tabHtml = '';
var contentHtml ='<div class="col-8">' +
                    '<div class="tab-content" id="nav-tabContent">';

function getVideoResults() {
  // Clear the video results after a new search is made  
  const tabResult = document.getElementById("tab_list");
  const contentResult = document.getElementById("info_list");
  tabResult.innerHTML = '';
  contentResult.innerHTML = '';

  url = getRequestUrl();  
  fetch(url).then(response => response.json()).then(data => {
    videos = data.items; 
    videos.forEach(function(video) {
      tabHtml = tabHtml.concat(addVideoTitle(video));
      contentResult.appendChild(addVideoInfo(video));
    });
    document.getElementById("tab_list").innerHTML = tabHtml;
  });
}

function addVideoTitle(video) {
  console.log(video.id);
  return '<a class="list-group-item list-group-item-action" id=list-' + video.id.videoId +'-tab' + ' data-toggle="list" href="#'+ video.id.videoId + '" role="tab">' + video.snippet.title + '</a>';
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
}

function addVideoInfo(video) {
  var infoDiv = document.createElement("div");
  infoDiv.className = "tab-pane fade";
  infoDiv.id = video.id.videoId ;
  infoDiv.role = "tabpanel";
  infoDiv.setAttribute("role", "tabpanel");
  infoDiv.setAttribute("aria-labelledby", 'list-' + video.id.videoId );
  var cardDiv = document.createElement("div");
  cardDiv.className = "card";
  cardDiv.style.width = "30rem";
  var cardBodyDiv = document.createElement("div");
  cardBodyDiv.className = "card-body";
  cardBodyDiv.innerHTML = '<p class="card-text">' + video.snippet.description + '</p';
  console.log(video.snippet.thumbnails);
  cardDiv.innerHTML = '<IMG class="card-img-top" src=' + video.snippet.thumbnails.default.url + ' alt="Card image cap">';
  cardDiv.appendChild(cardBodyDiv);
  infoDiv.appendChild(cardDiv);
  return infoDiv;
}

function getRequestUrl() {
  userSearchInput = document.getElementById('search-input').value;
  url = URL_STRUCTURE + "good morning"+ "&key=" + YOUTUBE_API_KEY;
  return url;
}
