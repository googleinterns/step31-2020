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

/*
 * Extracts video ID from the full URL	 * Extracts video ID from the full URL
  * Credit for RegEx method derived from top answer on: 
  * https://stackoverflow.com/questions/28735459/how-to-validate-youtube-url-in-client-side-in-text-box
 */	 


function extractYouTubeUrl(url) {	
  var videoId = "";	  
  var idLength = 11; // Length of video Id's in YouTube videos	
  if (url != undefined || url != '') {	 
    // This regular expression represents the different patterns that may occur in a YouTube URL
    var regExp = /^.*(youtu.be\/|v\/|u\/\w\/|e\/|embed\/|watch\?v=|\&v=|\?v=|\?vi=|v\&)([^#\&\?]*).*/;
    var match = url.match(regExp);
    // Check for proper format, focus on segment that contains videoId
    if (match && match[2].length == idLength) {
      videoId = match[2];
    }   
  }
  return videoId;
}