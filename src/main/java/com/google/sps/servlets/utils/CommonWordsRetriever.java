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

package com.google.sps.servlets.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Class provides commonly used words that should be ignored within the word map */
public class CommonWordsRetriever {
  // TODO: Parse text file for common words
  private static final List<String> commonWordsList =
      new ArrayList<>(
          Arrays.asList(
              "the", "it", "at", "and", "to", "we", "can", "are", "of", "is", "am", "i", "this",
              "was"));

  public static List<String> getCommonWords() {
    return new ArrayList<String>(commonWordsList);
  }
}
