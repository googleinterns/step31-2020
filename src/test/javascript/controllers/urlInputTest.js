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

// Test Urls derived from https://gist.github.com/rodrigoborgesdeoliveira/987683cfbfcc8d800192da1e73adc486
// These encompass the URL's that the parser is able to handle.
var outputUrls = ["dQw4w9WgXcQ","-wtIMTCHWuI","-wtIMTCHWuI","JdfC0C9V6ZI"];
var testUrls = ["youtube.com/watch?v=dQw4w9WgXcQ", "http://youtu.be/-wtIMTCHWuI",
    "http://www.youtube.com/oembed?url=http%3A//www.youtube.com/watch?v%3D-wtIMTCHWuI",
    "http://www.youtube.com/attribution_link?a=JdfC0C9V6ZI&u=%2Fwatch%3Fv%3DEhxJLojIE_o%26feature%3Dshare",
    ]

describe('The application controller', function() {
  for(i = 0; i < testUrls.length; i++) {
    it('ensures test url ' + i + ' returns expected url ' + i +'.', function() {
      // Iterator i could not be passed into the array in the loop, so a secondary iterator had to be used.
      expect(extractYouTubeUrl(testUrls[x])).toBe(outputUrls[x]);
      x++;
    });
  }
});


