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
const START_COLOR = ' #76A7FA';

google.charts.load('current', {'packages':['corechart']});
google.setOnLoadCallback(getChart);

window.onload = initCommentSlider;

function initCommentSlider(){
  const numCommentsSlider = document.getElementById(SLIDER_NAME);
  const numCommentsIndicator = document.getElementById('slider-output');
  numCommentsIndicator.innerText = numCommentsSlider.value;

  numCommentsSlider.oninput = function() {
    numCommentsIndicator.innerText = this.value;
  }
}

async function getYouTubeComments() {
  const urlInput = document.getElementById('link-input');
  const url = cleanseUrl(urlInput.value);
  const numComments = document.getElementById(SLIDER_NAME).value;
  const response = await fetch("/YouTubeComments?url="+url+"&numComments="+numComments);
  const comments = await response.json();
  return comments;
}

/**
 * Fetches data and adds to html
 */
async function getChart() {
  $('form').submit(async function() {
      document.getElementById('loading-img').style.display = 'block';
  
      // commentStats = await getYouTubeComments();
      averageScore = 0.1;
      averageMagnitude = 0.2;
      // sentimentBucketList = commentStats.sentimentBucketList;
  
      const CommentSentimentTable = new google.visualization.DataTable();
      CommentSentimentTable.addColumn('string', 'Sentiment Range');
      CommentSentimentTable.addColumn('number', 'Comment Count');
      CommentSentimentTable.addColumn(
          {'type': 'string', 'role': 'tooltip', 'p': {'html': true}});
      CommentSentimentTable.addColumn({type: 'string', role: 'style'});

  
      for (i = 0; i < 10; i++) {
        CommentSentimentTable.addRow(["rangeAsString",
          10,
          "toTooltipString(highestMagnitudeComments)", toColorStyle(i)]);
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

function adjust(amount) {
  return '#' + START_COLOR.replace(/^#/, '').replace(/../g, color => ('0'+Math.min(255, Math.max(0, parseInt(START_COLOR, 16) + amount)).toString(16)).substr(-2));
}
function toColorStyle(columnNum) {
  console.log(adjust(columnNum*10));
  return 'fill-color:blue; fill-opacity:' + (1-columnNum*(0.1));
}

