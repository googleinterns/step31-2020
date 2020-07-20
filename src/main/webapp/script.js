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
 
async function getYouTubeComments(currUrl) {
  const urlInput = currUrl; //document.getElementById('link-input');
  console.log(currUrl);
  const url = cleanseUrl(currUrl);
  const response = await fetch("/YouTubeComments?url="+url);
  const comments = await response.json();
  return comments;
}

/**
 * Fetches data and adds to html
 */
async function getChart(url) {
  $('.button').click(async function() {
    // After video is selected, hide the search results and display the loading icon
    document.getElementById("video-results").style.display = "none";
    document.getElementById('loading-img').style.display = "block";

    commentStats = await getYouTubeComments(url);
    console.log(commentStats);
    averageScore = commentStats.averageScore;
    averageMagnitude = commentStats.averageMagnitude;
    sentimentBucketList = commentStats.sentimentBucketList; 

    const CommentSentimentTable = new google.visualization.DataTable();
    CommentSentimentTable.addColumn('string', 'Sentiment Range');
    CommentSentimentTable.addColumn('number', 'Comment Count');
    CommentSentimentTable.addColumn({type: 'string', role:'tooltip', 'p': {'html': true}});
 
    for(i = 0; i < sentimentBucketList.length; i++) {
      currentSentimentBucket = sentimentBucketList[i];
      rangeAsString = convertRangeToString(currentSentimentBucket.intervalRange);
      highestMagnitudeComments = currentSentimentBucket.topNComments;

      CommentSentimentTable.addRow([rangeAsString, currentSentimentBucket.frequency, 
        toTooltipString(highestMagnitudeComments)]);
    }
 
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

    const averageContainer = document.getElementById('average-score-container');
    averageContainer.innerHTML = "Average Sentiment Score: " + averageScore;
  });
}
 
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