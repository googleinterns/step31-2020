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

/**
 * Initialize comment slider, used for choosing
 * the number of comments to be analyzed
 */
function initCommentSlider() {
  const numCommentsSlider = document.getElementById(SLIDER_NAME);
  const numCommentsIndicator = document.getElementById('slider-output');
  numCommentsIndicator.innerText = numCommentsSlider.value;

  numCommentsSlider.oninput = function() {
    numCommentsIndicator.innerText = this.value;
  };
}

/**
 * Retrieve the youtube comments given the URL
 * @param {String} url to pass into backend
 * @return {JSON} comments json object of comment objects
 */
async function getYouTubeComments(url) {
  url = extractYouTubeUrl(url);
  const numComments = document.getElementById(SLIDER_NAME).value;
  const response = await fetch('/YouTubeComments?url=' + url +
      '&numComments=' + numComments);
  const comments = await response.json();
  return comments;
}

/**
 * Wrapper function for preparing onClick function
 */
function onButtonPress() {
  $('#submit-link-btn').click(function() {
    try {
      $('#link-analysis > #error-surfacer').hide();
      showLoadingGif('link-analysis');  
      const urlInput = document.getElementById('link-input').value;
      updateUIWithVideoContext(urlInput, 'link-analysis');
      displayOverallResults(urlInput, 'link-analysis');
    } catch (err) {
      displayError(err, 'link-analysis');
    }
  });
}

/**
 * Sets error message to visible and gives details on specific error.
 * @param {String} err the error to display and log.
 * @param {String} divId the div to display error in
 */
function displayError(err, divId) {
  hideLoadingGif(divId);
  $('#' + divId + '> #error-surfacer').show();
  $('#' + divId + '> #error-details').html(err.message);
  console.log(err);
}

/**
 * Fetches data and adds to html
 * @param {String} url youtube url to retrieve comments
 * @param {string} divId id of div to be altered
 */
async function displayOverallResults(url, divId) {
  // Clear all analysis containers
  const averageContainer = $('#' + divId + '> #average-score-container').html('');
  const chartContainer = $('#' + divId + '> #chart-container');
  const wordCloudContainer = $('#' + divId + '> #word-cloud-container');
  clearElement(chartContainer.attr('id'), divId);
  clearElement(wordCloudContainer.attr('id'), divId);
  try {
    commentStats = await getYouTubeComments(url);
    sentimentBucketList = commentStats.sentimentBucketList;
    wordFrequencyMap = commentStats.wordFrequencyMap;
    displaySentimentBucketChart(sentimentBucketList, divId);
    displayWordCloudChart(wordFrequencyMap, divId);
    
    hideLoadingGif(divId);

    averageScore = commentStats.averageScore;
    averageContainer.html('Average Sentiment Score: ' + averageScore);
  } catch (err) {
    err.message = 'Error in overall display: ' + err.message;
    displayError(err, divId);
  }
}

/**
 * Create a bar chart of sentiment score interval, frequency
 * and high magnitude comments
 * @param {Array<sentimentBucket>} sentimentBucketList
 * @param {string} divId id of div to be altered
 */
function displaySentimentBucketChart(sentimentBucketList, divId) {
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

  const view = new google.visualization.DataView(CommentSentimentTable);
  const chart = new google.visualization.ColumnChart($('#' + divId + '> #chart-container')[0]);
  chart.draw(view, options);
}

/**
 * Create a word cloud based on the number of appearance for each word
 * @param {Map} wordFrequencyMap that contains top words
 * @param {string} divId id of div to be altered
 */
function displayWordCloudChart(wordFrequencyMap, divId) {
  const data = [];
  Object.keys(wordFrequencyMap).forEach((wordKey) =>
    data.push({'x': wordKey, 'value': wordFrequencyMap[wordKey]}));
  // Create a tag cloud chart
  const chart = anychart.tagCloud(data);

  chart.title('Most Common Words in Comments');
  // Set array of angles to 0, make all the words display horizontally
  chart.angles([0]);
  chart.container($('#' + divId + '> #word-cloud-container')[0]);
  chart.draw();
};

/**
 * Get the comment content with top high magnitude.
 * @param {List<UserComment>} userComments a sentiment buckets list
 *                           with userComment, score, and maginitude
 * @return {String} Top high comment message.
 */
function toTooltipString(userComments) {
  return userComments.map((comment) => userCommentAsString(comment))
      .join('<br>');
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

/**
 * Convert a Range object to a string
 * @param {JSON} range json object of java Range object
 * @return {String} the given object in string form
 */
function convertRangeToString(range) {
  return range.inclusiveStart + ' to ' + range.exclusiveEnd;
}

/**
 * Display loading image
 * @param {string} divId id of div to be altered
 */
function showLoadingGif(divId) {
  $('#' + divId + '> #video-results-loading-container > #loading-img').show();
}

/**
 * Hide loading image
 * @param {string} divId id of div to be altered
 */
function hideLoadingGif(divId) {
  $('#' + divId + '> #video-results-loading-container > #loading-img').hide();
}

/**
 * Hide analysis from search tab
 */
function hideSearchInfo() {
  $('#search-analysis').hide();
  $('#link-analysis').show();
}

/**
 * Hide analysis from link tab
 */
function hideLinkInfo() {
  $('#link-analysis').hide();
  $('#search-analysis').show();
}
