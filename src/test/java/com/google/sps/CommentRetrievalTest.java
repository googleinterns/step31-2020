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

import com.google.api.services.youtube.model.*;
import com.google.sps.servlets.utils.YouTubeCommentRetriever;
import com.google.sps.servlets.utils.Statistics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CommentRetrievalTest {

@Before
public void setup() {}

// The simplest case: extract 100 comments from a video with more than 100 comments
@Test 
public void testDefaultBehaviour() {
  List<CommentThread> comments = YouTubeCommentRetriever.retrieveComments("dQw4w9WgXcQ",100);
  Assert.assertEquals(comments.size(), 100);
}

// Test that class properly consolidates a list of comments greater than the max allowed per thread
@Test 
public void testExcessHundredComments() {
  List<CommentThread> comments = YouTubeCommentRetriever.retrieveComments("dQw4w9WgXcQ",200);
  Assert.assertEquals(comments.size(), 200);
}

// Ensure that a crash does not occur when loading more comments than there are on a video
@Test
public void doesNotAttemptRetrieveExcess() {
  // This url leads to a video that doesn't have >200 comments and probably won't ever.
  List<CommentThread> comments = YouTubeCommentRetriever.retrieveComments("cA-arJ0T6L4",200);
  Assert.assertTrue(comments.size() < 200);
}

// When a URL that is not real is inputted, an empty list is returned.
@Test 
public void handlesErrantVideoId() {
    List<CommentThread> comments = YouTubeCommentRetriever.retrieveComments("NOT_A_URL",200);
  Assert.assertEquals(comments.size(), 0);
}

}