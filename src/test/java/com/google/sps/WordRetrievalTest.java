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

package com.google.sps;

import com.google.sps.servlets.utils.CommonWordsRetriever;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** JUnit test for Word Retriever class */
@RunWith(JUnit4.class)
public class WordRetrievalTest {

  @Test
  public void testCommonWordParse() {
    List<String> wordList = CommonWordsRetriever.getCommonWords();
    int expectedWordCount = 22;
    Assert.assertEquals(expectedWordCount, wordList.size());
  }

  @Test
  public void testCommonWordContent() {
    List<String> wordList = CommonWordsRetriever.getCommonWords();
    List<String> expectedList =
        new ArrayList<String>(
            Arrays.asList(
                "the", "it", "at", "and", "to", "we", "can", "are", "of", "its", "it", "am", "my",
                "or", "by", "for", "i", "my", "this", "was", "is", "a"));
    Assert.assertEquals(expectedList, wordList);
  }
}
