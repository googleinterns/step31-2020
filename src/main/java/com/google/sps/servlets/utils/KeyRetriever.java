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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KeyRetriever {
  private static String keyPath =
      System.getProperty("user.dir") + System.getProperty("resources-folder") + "/apiKey";
  /**
   * Reads the API Key from a file
   *
   * @return {String} the api key
   */
  public static String getApiKey() {
    try {
      return Files.readAllLines(Paths.get(keyPath), StandardCharsets.US_ASCII).get(0);
    } catch (IOException e) {
      System.err.println("Error: API Key File could not be found or read.");
      e.printStackTrace();
      return null;
    }
  }
}
