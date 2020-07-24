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
 * @param url: url of the video to be analyzed
 * @returns: just the video id of that video
 * Credit for RegEx method derived from top answer on: 
 * https://stackoverflow.com/questions/28735459/how-to-validate-youtube-url-in-client-side-in-text-box
 * TODO: If YouTube URL is not found, display error to user.
 */	 
function extractYouTubeUrl(url) {
  if (url != undefined || url != '') {
    // This regular expression represents 
    // The different patterns that may occur in a YouTube URL
    const regExp = /^.*(youtu.be\/|v\/|u\/\w\/|e\/|embed\/|watch\?v=|\&v=|\?v=|\?vi=|v\&)([^#\&\?]*).*/;
    const match = url.match(regExp);
    if(match == null || match.length < 3) {
      return null;
    }
    // Return segment that contains videoId
    return match[2];
  }
}
