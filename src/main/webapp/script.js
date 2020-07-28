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

const CHART_WIDTH = 800;
const CHART_HEIGHT = 400;
const SLIDER_NAME = 'num-comments-input';

google.charts.load('current', {'packages': ['corechart']});
google.setOnLoadCallback(onButtonPress);

window.onload = initCommentSlider;

function initCommentSlider() {
  const numCommentsSlider = document.getElementById(SLIDER_NAME);
  const numCommentsIndicator = document.getElementById('slider-output');
  numCommentsIndicator.innerText = numCommentsSlider.value;

  numCommentsSlider.oninput = function() {
    numCommentsIndicator.innerText = this.value;
  };
}

/**
 * Retreive the youtube comments from the url.
 */
async function getYouTubeComments(url) {
  url = cleanseUrl(url);
  const numComments = document.getElementById(SLIDER_NAME).value;
  const response = await fetch("/YouTubeComments?url="+url +"&numComments="+numComments);
  const comments = await response.json();
  return comments;
}

/**
 * Wrapper function for preparing onClick function
 */
function onButtonPress() {
  $('.submitBtn').click(function() {
    showLoadingGif();
    getChart();
  });
}

/**
 * Fetches data and adds to html
 * @param {String} url youtube url to retrieve comments
 */
async function getChart(url) {
  // Clear all analysis containers
  const averageContainer = document.getElementById('average-score-container');
  averageContainer.innerHTML = '';
  clearElement('chart-container');
  clearElement('word-cloud-container');

  if (url == undefined) {
    // Link input
    url = document.getElementById('link-input').value;
  } else {
    // Keyword search
    clearElement('video-results');
  }

  commentStats = await getYouTubeComments(url);
  sentimentBucketList = commentStats.sentimentBucketList;
  wordFrequencyMap = commentStats.wordFrequencyMap;
  displaySentimentBucketChart(sentimentBucketList);
  displayWordCloudChart(wordFrequencyMap);

  averageScore = commentStats.averageScore;
  averageContainer.innerHTML = 'Average Sentiment Score: ' + averageScore;
}

/**
 * Create a bar chart of sentiment score interval, frequency
 * and high magnitude comments
 * @param {Array<sentimentBucket>} sentimentBucketList
 */
function displaySentimentBucketChart(sentimentBucketList) {
  const CommentSentimentTable = new google.visualization.DataTable();
  CommentSentimentTable.addColumn('string', 'Sentiment Range');
  CommentSentimentTable.addColumn('number', 'Comment Count');
  CommentSentimentTable.addColumn(
      {'type': 'string', 'role': 'tooltip', 'p': {'html': true}});

  for (i = 0; i < sentimentBucketList.length; i++) {
    currentSentimentBucket = sentimentBucketList[i];
    rangeAsString = convertRangeToString(
        currentSentimentBucket.intervalRange);
    highestMagnitudeComments = currentSentimentBucket.topNComments;

    CommentSentimentTable.addRow([rangeAsString,
      currentSentimentBucket.frequency,
      toTooltipString(highestMagnitudeComments)]);
  }

  const options = {
    'title': 'Comment Sentiment Range',
    'width': CHART_WIDTH,
    'height': CHART_HEIGHT,
    'bar': {groupWidth: '100'},
    'tooltip': {isHtml: true},
  };
  // Hide loading image once chart is drawn
  document.getElementById('loading-img').style.display = 'none';

  const view = new google.visualization.DataView(CommentSentimentTable);
  const chart = new google.visualization.ColumnChart(
      document.getElementById('chart-container'));
  chart.draw(view, options);
}

/**
 * Create a word cloud based on the number of appearance for each word
 * @param {Map<String:Integer>} wordFrequencyMap Map that contains
 *                                top popular words and its appearance
 */
function displayWordCloudChart(wordFrequencyMap) {
  const data = [];
  Object.keys(wordFrequencyMap).forEach((wordKey) =>
    data.push({'x': wordKey, 'value': wordFrequencyMap[wordKey]}));
  // Create a tag cloud chart
  const chart = anychart.tagCloud(data);

  chart.title('Most Common Words in Comments');
  // Set array of angles to 0, make all the words display horizontally
  chart.angles([0]);
  chart.container('word-cloud-container');
  chart.draw();
};

/**
 * Get the comment content with top high magnitude.
 * @param {List<UserComment>} userComments a sentiment buckets list
 *                           with userComment, score, and maginitude
 * @return {String} Top high comment message.
 */
function toTooltipString(userComments) {
  return userComments.map(comment => userCommentAsString(comment)).join("<br>");
}

/**
 * Convert a userComment object to html format
 * @param {*} comment a user comment with high magnitude score
 * @return {String} HTML format to display its message and magnitude
 */
function userCommentAsString(comment) {
  commentMagnitude = comment.magnitudeScore;
  return comment.commentMsg + '<br> Magnitude Score: ' + commentMagnitude;
}

function convertRangeToString(range) {
  return range.inclusiveStart + ' to ' + range.exclusiveEnd;
}

/**
 * Convert a userComment object to html format
 */
async function showLoadingGif() {
  const loadContainer = document.getElementById('loading-container');
  while (loadContainer.lastElementChild) {
    loadContainer.removeChild(loadContainer.lastElementChild);
  }

  const gif = document.createElement('div');
  gif.className = 'spinner-border text-dark';
  gif.id = 'loading-img';
  gif.setAttribute('role', 'status');

  const gifSpan = document.createElement('div');
  gifSpan.className = 'sr-only';

  gif.appendChild(gifSpan);
  document.getElementById('loading-container').appendChild(gif);  
}
