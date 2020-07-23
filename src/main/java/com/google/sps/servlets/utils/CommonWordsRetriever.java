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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Class that provides commonly used words that should be ignored within the word map */
public class CommonWordsRetriever {
  private static final List<String> commonWordsList = populateWordList();

  /** Populate a list of strings from a text file containing the words to ignore in the word map */
  private static List<String> populateWordList() {
    List<String> commonWords = new ArrayList<String>();
    try {
      File file =
          new File(
              System.getProperty("user.dir")
                  + "/src/main/java/com/google/sps/servlets/utils/resources/common_words.txt");
      Scanner sc = new Scanner(file);
      while (sc.hasNextLine()) {
        commonWords.add(sc.nextLine());
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Unable to read file.", e);
    }
    return commonWords;
  }

  public static List<String> getCommonWords() {
    return new ArrayList<String>(commonWordsList);
  }
}
