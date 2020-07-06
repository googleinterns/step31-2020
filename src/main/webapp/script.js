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
    commentStats = await getYouTubeComments();
    averageScore = commentStats.averageScore;
    aggregatedValues = commentStats.aggregateValues; 

    const CommentSentimentTable = new google.visualization.DataTable();
    CommentSentimentTable.addColumn('string', 'SentimentRange');
    CommentSentimentTable.addColumn('number', 'CommentCount');

    currentLabel = LOWEST_SCORE;
    // The json keys (ranges of scores) are sorted through their starting values
    // using subtraction comparison  
    Object.keys(aggregatedValues).sort((a, b) => a - b).forEach(function(key) {
      CommentSentimentTable.addRows([
          [key, null],
          [null, aggregatedValues[key]]
      ]);
      currentLabel += INTERVAL;
    });
    CommentSentimentTable.addRow([HIGHEST_SCORE.toString(), null]);

    const options = {
      'title': 'Comment Sentiment Range',
      'width': CHART_WIDTH,
      'height':CHART_HEIGHT,
      'bar': {groupWidth: "100"}
    };
    const chart = new google.visualization.ColumnChart(
        document.getElementById('chart-container'));
    chart.draw(CommentSentimentTable, options);
  });
}

