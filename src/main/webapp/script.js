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

google.charts.load('current', {'packages': ['corechart']});
google.setOnLoadCallback(getChart);

/**
 * Fetches comment data from video ID
 */
async function getYouTubeComments() {
  const urlInput = document.getElementById('link-input');
  const url = cleanseUrl(urlInput.value);
  const response = await fetch('/YouTubeComments?url='+url);
  const comments = await response.json();
  return comments;
}

/**
 * Fetches data and adds to html
 */
async function getChart() {
  $('form').submit(async function() {
    document.getElementById('loading-img').style.display = 'block';
    commentStats = await getYouTubeComments();
    averageScore = commentStats.averageScore;
    averageMagnitude = commentStats.averageMagnitude;
    sentimentBucketList = commentStats.sentimentBucketList;

    const CommentSentimentTable = new google.visualization.DataTable();
    CommentSentimentTable.addColumn('number', 'InclusiveStart');
    CommentSentimentTable.addColumn('string', 'SentimentRange');
    CommentSentimentTable.addColumn('number', 'CommentCount');

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
      'tooltip': {isHtml: true}
    };

    // Hide loading image once chart is drawn
    document.getElementById('loading-img').style.display = 'none';

    const view = new google.visualization.DataView(CommentSentimentTable);
    const chart = new google.visualization.ColumnChart(
        document.getElementById('chart-container'));
    chart.draw(view, options);

    const averageContainer = document.getElementById('average-score-container');
    averageContainer.innerHTML = 'Average Sentiment Score: ' + averageScore;
  });
}

function toTooltipString(userComments) {
  return userComments.map(comment => userCommentAsString(comment)).join("<br>");
}

function userCommentAsString(comment) {
  commentMagnitude = comment.magnitude;  
  return comment.commentMsg + '<br> Magnitude Score: ' + commentMagnitude;
}

function convertRangeToString(range) {
  return range.inclusiveStart + ' to ' + range.exclusiveEnd; 
}
