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

var numCommentsSlider;

google.charts.load('current', {'packages':['corechart']});
google.setOnLoadCallback(getChart)

window.onload = init;

function init(){
  numCommentsSlider = document.getElementById('num-comments-input');
  var numCommentsIndicator = document.getElementById('slider-output');

  numCommentsSlider.oninput = function() {
    numCommentsIndicator.innerText = this.value;
  }
}

async function getYouTubeComments() { 
  const urlInput = document.getElementById('link-input');
  const url = cleanseUrl(urlInput.value);
  const numComments = numCommentsSlider.value;
  const response = await fetch("/YouTubeComments?url="+url+"&numComments="+numComments);
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
    averageScore = commentStats.averageScore;
    aggregateValues = commentStats.aggregateValues; 

    const CommentSentimentTable = new google.visualization.DataTable();
    CommentSentimentTable.addColumn('number', 'InclusiveStart');
    CommentSentimentTable.addColumn('string', 'SentimentRange');
    CommentSentimentTable.addColumn('number', 'CommentCount');

    // The json keys (ranges of scores) are sorted through their starting values
    Object.keys(aggregateValues).forEach(function(key) {
      var inclusiveStart = getRangeInclusiveStart(key);  
      var exclusiveEnd = getRangeExclusiveEnd(key);
      CommentSentimentTable.addRow([inclusiveStart, inclusiveStart + ' to ' + exclusiveEnd, aggregateValues[key]]);  
    });

    const options = {
      'title': 'Comment Sentiment Range',
      'width': CHART_WIDTH,
      'height':CHART_HEIGHT,
      'bar': {groupWidth: "100"}
    };

    document.getElementById('loading-img').style.display = "none";  

    CommentSentimentTable.sort({column: 0, desc: false}); 
    var view = new google.visualization.DataView(CommentSentimentTable);
    view.setColumns([1, 2]); 

    const chart = new google.visualization.ColumnChart(
        document.getElementById('chart-container'));
    chart.draw(view, options);    

    const averageContainer = document.getElementById('average-score-container');
    averageContainer.innerHTML = "Average Sentiment Score: " + averageScore;
  });
}

function getRangeExclusiveEnd(rangeString) {
  rangeString.trim();
  return Number(rangeString.substring(rangeString.indexOf(',') + 1, rangeString.length - 1));
}
 
function getRangeInclusiveStart(rangeString) {
  rangeString.trim();
  return Number(rangeString.substring(1, rangeString.indexOf(',')));
}
