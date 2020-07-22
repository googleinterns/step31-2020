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

// Import script
var script = document.createElement('script');
script.src = 'src/main/webapp/common_util.js'
var x = 0

// Test Urls sampled from https://gist.github.com/rodrigoborgesdeoliveira/987683cfbfcc8d800192da1e73adc486
// These encompass the URL's that the parser is able to handle.
var outputUrls = ["dQw4w9WgXcQ","-wtIMTCHWuI", "yZv2daTWRZU", "QdK8U-VIH_o",
    "0zM3nApSvMg","0zM3nApSvMg","1p3vcRhsYGo", "dQw4w9WgXcQ","dQw4w9WgXcQ",
    "dQw4w9WgXcQ", "6L3ZvIMwZFM", "oTJRivZTMLs", "oTJRivZTMLs"];
var testUrls = ["youtube.com/watch?v=dQw4w9WgXcQ", "http://youtu.be/-wtIMTCHWuI",
    "https://www.youtube.com/watch?v=yZv2daTWRZU&feature=em-uploademail",
    "https://www.youtube.com/user/IngridMichaelsonVEVO#p/a/u/1/QdK8U-VIH_o",
    "https://www.youtube.com/watch?v=0zM3nApSvMg#t=0m10s",
    "https://www.youtube.com/embed/0zM3nApSvMg?rel=0",
    "http://www.youtube.com/user/Scobleizer#p/u/1/1p3vcRhsYGo",
    "http://youtube.com/?vi=dQw4w9WgXcQ&feature=youtube_gdata_player",
    "http://www.youtube.com/e/dQw4w9WgXcQ",
    "http://www.youtube.com/watch?feature=player_embedded&v=dQw4w9WgXcQ",
    "http://www.youtube-nocookie.com/v/6L3ZvIMwZFM?version=3&hl=en_US&rel=0",
    "http://youtube.com/watch?vi=oTJRivZTMLs&feature=channel",
    "http://www.youtube.com/user/dreamtheater#p/u/1/oTJRivZTMLs"]

describe('The application controller', function() {
  for(i = 0; i < testUrls.length; i++) {
    it('ensures test url ' + i + ' returns expected url ' + i +'.', function() {
      // Iterator i could not be passed into the array in the loop, so a secondary iterator had to be used.
      expect(extractYouTubeUrl(testUrls[x])).toBe(outputUrls[x]);
      x++;
    });
  }
});


