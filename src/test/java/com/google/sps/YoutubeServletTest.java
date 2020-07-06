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

import com.google.sps.servlets.YoutubeServlet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** This is a JUnit test for the Youtube Servlet */
@RunWith(JUnit4.class)
public class YoutubeServletTest {
  private YoutubeServlet servlet;
  private MockHttpServletRequest mockRequest;
  private MockHttpServletResponse mockResponse;

  private String testUrl = "dQw4w9WgXcQ";

  @Before
  public void setUp() throws Exception {
    servlet = new YoutubeServlet();
    mockRequest = new MockHttpServletRequest();
    mockResponse = new MockHttpServletResponse();
  }

  @Test
  public void testDoesntCrash() throws Exception {
    mockRequest.addParameter("url", testUrl);
    servlet.doGet(mockRequest, mockResponse);
  }
}
