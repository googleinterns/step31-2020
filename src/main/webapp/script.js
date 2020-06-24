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

google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(getChart);

const CHART_WIDTH = 800;
const CHART_HEIGHT = 400;

/**
 * Fetches data and adds to html
 */
function getChart() {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Range');
    data.addColumn('number', 'Count');
    // TODO: change hardcoded data to sentiment scores (once that part is finished)
    data.addRows([
        ['-1.0', null],
        [null, 1],
        ['-0.8', null],
        [null, 1],
        ['-0.6', null],
        [null, 4],
        ['-0.4', null],
        [null, 5],
        ['-0.2', null],
        [null, 10],
        ['0.0', null],
        [null, 10],
        ['0.2', null],
        [null, 10],
        ['0.4', null],
        [null, 10],
        ['0.6', null],
        [null, 15],
        ['0.8', null],
        [null, 15],
        ['1.0', null]
    ]);

    const options = {
      'title': 'Comment Sentiment Range',
      'width': CHART_WIDTH,
      'height':CHART_HEIGHT
     };
    const chart = new google.visualization.ColumnChart(
        document.getElementById('chart-container'));
    chart.draw(data, options);
}