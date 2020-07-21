// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

const CHART_WIDTH = 800;
const CHART_HEIGHT = 400;

google.charts.load('current', {'packages': ['corechart']});
google.setOnLoadCallback(getChart);

/**
 * Retreive the youtube comments from the url.
 */
async function getYouTubeComments() {
  const urlInput = document.getElementById('link-input');
  const url = cleanseUrl(urlInput.value);
  const response = await fetch('/YouTubeComments?url='+url);
  const comments = await response.json();
  return comments;
}

/**
 *  Fetches data and adds to html.
 */
async function getChart() {
  $('form').submit(async function() {
    document.getElementById('loading-img').style.display = 'block';
    commentStats = await getYouTubeComments();
    sentimentBucketList = commentStats.sentimentBucketList;
    wordFrequencyMap = commentStats.wordFrequencyMap;

    getBarChart(sentimentBucketList);
    getWordCloudChart(wordFrequencyMap);
    console.log(wordFrequencyMap);
    averageScore = commentStats.averageScore;
    const averageContainer = document.getElementById('average-score-container');
    averageContainer.innerHTML = 'Average Sentiment Score: ' + averageScore;
  });
}

/**
 * Create a bar chart of sentiment score interval, frequency
 * and high magnitude comments
 * @param {Array<sentimentBucket>} sentimentBucketList
 */
function getBarChart(sentimentBucketList) {
  const CommentSentimentTable = new google.visualization.DataTable();
  CommentSentimentTable.addColumn('number', 'InclusiveStart');
  CommentSentimentTable.addColumn('string', 'SentimentRange');
  CommentSentimentTable.addColumn('number', 'CommentCount');

  // The json keys (ranges of scores) are sorted through their starting values
  sentimentBucketList.forEach((sentimentBucket) => {
    const inclusiveStart = sentimentBucket.intervalRange.inclusiveStart;
    const exclusiveEnd = sentimentBucket.intervalRange.exclusiveEnd;
    CommentSentimentTable.addRow(
        [inclusiveStart, inclusiveStart + ' to ' + exclusiveEnd,
          sentimentBucket.frequency]);
  });

  const options = {
    'title': 'Comment Sentiment Range',
    'width': CHART_WIDTH,
    'height': CHART_HEIGHT,
    'bar': {groupWidth: '100'},
    'tooltip': {isHtml: true},
  };

  // Hide loading image once chart is drawn
  document.getElementById('loading-img').style.display = 'none';

  CommentSentimentTable.sort({column: 0, desc: false});
  const view = new google.visualization.DataView(CommentSentimentTable);
  view.setColumns([1, 2]);

  const chart = new google.visualization.ColumnChart(
      document.getElementById('chart-container'));
  chart.draw(view, options);
}

/**
 * Create a word cloud based on the number of appearance for each word
 * @param {Map<String:Integer>} wordFrequencyMap Map that contains
 *                                top popular words and its appearance
 */
function getWordCloudChart(wordFrequencyMap) {
  const data = [];
  Object.keys(wordFrequencyMap).forEach((wordKey) =>
    data.push({'x': wordKey, 'value': wordFrequencyMap[wordKey]}));
  // Create a tag cloud chart
  const chart = anychart.tagCloud(data);

  chart.title('Most Popular Comments');
  // Set array of angles to 0, make all the words display horizontally
  chart.angles([0]);
  chart.container('word-cloud');
  chart.draw();
};
/**
 * Get the comment content with top high magnitude.
 * @param {List<UserComment>} userComments a sentiment buckets list
 *                           with userComment, score, and maginitude
 * @return {String} Top high comment message.
 */
function toTooltipString(userComments) {
  return userComments.map((comment) =>
    userCommentAsString(comment)).join('<br>');
}

/**
 * Convert a userComment object to html format
 * @param {*} comment a user comment with high magnitude score
 * @return {String} HTML format to display its message and magnitude
 */
function userCommentAsString(comment) {
  commentMagnitude = comment.magnitude;
  return comment.commentMsg + '<br> Magnitude Score: ' + commentMagnitude;
}

