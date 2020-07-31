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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Class that provides commonly used words that should be ignored within the word map */
public class CommonWordsRetriever {
  private static String filePath = "/common_words.txt";
  private static final List<String> commonWordsList = populateWordList();

  /** Populate a list of strings from a text file containing the words to ignore in the word map */
  private static List<String> populateWordList() {
    List<String> commonWords = new ArrayList<String>();
    String fileName =
        System.getProperty("user.dir") + System.getProperty("resources-folder") + filePath;
    System.out.println(fileName);
    try {
      return Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Unable to read file.", e);
    }
  }

  public static List<String> getCommonWords() {
    return new ArrayList<String>(commonWordsList);
  }

  //  /**
  //   * Check if the current environment is JUnit Test or not
  //   *
  //   * @return True if in Junit test; otherwise false
  //   */
  //  public static boolean isJUnitTest() {
  //    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
  //    List<StackTraceElement> list = Arrays.asList(stackTrace);
  //    for (StackTraceElement element : list) {
  //      if (element.getClassName().startsWith("org.junit.")) {
  //        return true;
  //      }
  //    }
  //    return false;
  //  }
}
