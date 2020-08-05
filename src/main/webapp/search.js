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

const URL_STRUCTURE = 'https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&maxResults=5&q=';
const YOUTUBE_API_KEY = 'AIzaSyDYfjWcy1hEe0V7AyaYzgIQm_rT-9XbiGs';

/**
 * Retrieve the most relevant video results from youtube url
 */
function getVideoResults() {
  disableLoadingButton('search-button');
  const tabResult = document.getElementById('tab_list');
  const contentResult = document.getElementById('info_list');
  clearElement('tab_list');
  clearElement('info_list');
  url = getRequestUrl();
  fetch(url).then((response) => response.json()).then((data) => {
    videos = data.items;
    const tabFragment = document.createDocumentFragment();
    const contentFragment = document.createDocumentFragment();

    videos.forEach(function(video) {
      tabFragment.appendChild(addVideoTitle(video));
      contentFragment.appendChild(addVideoInfo(video));
    });
    tabResult.appendChild(tabFragment);
    contentResult.appendChild(contentFragment);
  });
}

/**
 * Add video title to the list group.
 * @param {Json} video object that contains information
 *                     retrieved from Youtube Service
 * @return {String} html format of video's title and channel for list group
 */
function addVideoTitle(video) {
  const item = document.createElement('a');
  item.className = 'list-group-item list-group-item-action';
  item.href = '#' + video.id.videoId;
  item.id = 'list-' + video.id.videoId + '-tab';
  item.setAttribute('data-toggle', 'list');
  item.setAttribute('role', 'tab');
  item.innerHTML = '<h5 class="mb-1">' + video.snippet.title + '</h5>' +
  '<p class="mb-1">Channel: ' + video.snippet.channelTitle + '</p>';
  return item;
}

/**
 * Add video title to the explanation part of list group.
 * @param {Json} video object that contains information
 *                     retrieved from Youtube Service
 * @return {String} html format of video's thumnail pictures and descriptions
 */
function addVideoInfo(video) {
  // Create a separate card for explanation area.
  const infoDiv = document.createElement('div');
  infoDiv.className = 'tab-pane fade';
  infoDiv.id = video.id.videoId;
  infoDiv.role = 'tabpanel';
  infoDiv.setAttribute('role', 'tabpanel');
  infoDiv.setAttribute('aria-labelledby', 'list-' + video.id.videoId);
  const cardDiv = document.createElement('div');
  cardDiv.className = 'card';
  cardDiv.style.width = '30rem';

  // Include the video description and image for the card body.
  const cardBodyDiv = document.createElement('div');
  cardBodyDiv.className = 'card-body';
  cardBodyDiv.innerHTML = '<p class="card-text">' +
    video.snippet.description + '</p>';
  cardDiv.innerHTML = '<iframe id="search-video-frame" width="480" height="315"src=' + 'https://www.youtube.com/embed/'+video.id.videoId+ '?controls=1&disablekb=1;' +
    '></iframe>';

  // Create button to select the video
  const button = document.createElement('INPUT');
  const youtubeUrl = 'https://youtube.com/watch?v=' + video.id.videoId;
  button.setAttribute('type', 'button');
  button.id = 'select-video-btn';
  button.addEventListener('click', async () => {
    try {
      document.getElementById('select-video-btn').disabled = true;
      document.getElementById('search-error-surfacer').style.display = 'none';
      clearElement('tab_list');
      showLoadingGif('search-');
      updateUIWithVideoContext(youtubeUrl, 'search-');
      await displayOverallResults(youtubeUrl, 'search-');
      enableLoadingButton('search-button');
    } catch (err) {
      displayError(err, 'search-');
    }
  });
  button.value = 'Select!';

  // Append the card into general overall list group explanation.
  cardBodyDiv.appendChild(button);
  cardDiv.appendChild(cardBodyDiv);
  infoDiv.appendChild(cardDiv);
  return infoDiv;
}

/**
 * Create the request url to retrieve video information.
 * @return {String} url with API key and keyword to search.
 */
function getRequestUrl() {
  userSearchInput = document.getElementById('search-input').value;
  url = URL_STRUCTURE + userSearchInput + '&key=' + YOUTUBE_API_KEY;
  return url;
}

