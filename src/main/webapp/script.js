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
const HIGHEST_SCORE = 1.0;
const LOWEST_SCORE = -1.0;
const INTERVAL = 0.2;

google.charts.load('current', {'packages':['corechart']});
google.setOnLoadCallback(getChart)

async function getYouTubeComments() {
  const urlInput = document.getElementById('link-input');
  const url = cleanseUrl(urlInput.value);
  const response = await fetch("/YouTubeComments?url="+url);
  const comments = await response.json();
  return comments;
}

/*
 * Extracts video id from full url
 */
function cleanseUrl(url) {
  // Split web address from parameters, extract first parameter
  // TODO: Add checks to make this work if video is not first parameter.
  var videoId = url.split("?");
  videoId = (videoId.length > 1) ? videoId[1].split("&")[0] : videoId[0];

  // If param name present, remove it to isolate video Id.
  videoId = videoId.replace("v=", "");

  return videoId;
}

/**
 * Fetches data and adds to html
 */
async function getChart() {
  $('form').submit(async function() {
    document.getElementById('loading-img').style.display = "block";
    commentStats = await getYouTubeComments();
    sentimentBucketList = commentStats.sentimentBucketList;
    wordFrequencyMap = commentStats.wordFrequencyMap;

    // getBarChart(sentimentBucketList);
    console.log(commentStats);
    getWordCloudChart(wordFrequencyMap);
    averageScore = commentStats.averageScore;
    const averageContainer = document.getElementById('average-score-container');
    averageContainer.innerHTML = "Average Sentiment Score: " + averageScore;
    }
  );
}

function getBarChart(sentimentBucketList) {

    const CommentSentimentTable = new google.visualization.DataTable();
    CommentSentimentTable.addColumn('number', 'InclusiveStart');
    CommentSentimentTable.addColumn('string', 'SentimentRange');
    CommentSentimentTable.addColumn('number', 'CommentCount');

    // The json keys (ranges of scores) are sorted through their starting values
    sentimentBucketList.forEach(sentimentBucket => {
      var inclusiveStart = sentimentBucket.intervalRange.inclusiveStart;
      var exclusiveEnd = sentimentBucket.intervalRange.exclusiveEnd;
      CommentSentimentTable.addRow([inclusiveStart, inclusiveStart + ' to ' + exclusiveEnd, sentimentBucket.frequency]);
    });

      CommentSentimentTable.addRow([rangeAsString, currentSentimentBucket.frequency,
        toTooltipString(highestMagnitudeComments)]);

    const options = {
      'title': 'Comment Sentiment Range',
      'width': CHART_WIDTH,
      'height':CHART_HEIGHT,
      'bar': {groupWidth: "100"},
      'tooltip': {isHtml: true}
    };

    var view = new google.visualization.DataView(CommentSentimentTable);
    const chart = new google.visualization.ColumnChart(
        document.getElementById('chart-container'));
    chart.draw(view, options);
}

function getWordCloudChart(wordFrequencyMap) {

  var data = [];
  Object.keys(wordFrequencyMap).forEach(wordKey => data.push({"x": wordKey, "value": wordFrequencyMap[wordKey]}));
  // create a tag cloud chart
  var chart = anychart.tagCloud(data);

  // set the chart title
  chart.title('Most Popular Comments')
  // set array of angles, by which words will be placed
  chart.angles([0])

  // display chart
  chart.container("word-cloud");
  chart.draw();
};

function toTooltipString(userComments) {
  return userComments.map(comment => userCommentAsString(comment)).join("<br>");
}

function userCommentAsString(comment) {
  commentMagnitude = comment.magnitude;
  return comment.commentMsg + "<br> Magnitude Score: " + commentMagnitude;
}

function convertRangeToString(range) {
  return range.inclusiveStart + " to " + range.exclusiveEnd;
}
