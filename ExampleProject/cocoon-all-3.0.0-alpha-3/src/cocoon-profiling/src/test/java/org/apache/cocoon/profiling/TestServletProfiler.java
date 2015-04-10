/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.profiling;

import static org.junit.Assert.*;

import org.apache.cocoon.profiling.profiler.ServletProfiler;
import org.junit.Before;
import org.junit.Test;

public class TestServletProfiler {

    private ServletProfiler servletProfiler;

    @Before
    public void setup() {
        this.servletProfiler = new ServletProfiler();
        this.servletProfiler.setMountPath("/myproject/");
    }

    @Test
    public void testRelativeUrl() {
        assertEquals("cocoon-profiling/ID", this.servletProfiler.createRelativeUrl(
                "http://localhost/myproject/home.html", "ID"));

        assertEquals("../cocoon-profiling/ID", this.servletProfiler.createRelativeUrl(
                "http://localhost/myproject/foo/home.html", "ID"));

        assertEquals("../../cocoon-profiling/ID", this.servletProfiler.createRelativeUrl(
                "http://localhost/myproject/foo/bar/home.html", "ID"));

        assertEquals("../../../cocoon-profiling/ID", this.servletProfiler.createRelativeUrl(
                "http://localhost/myproject/foo/bar/buz/home.html", "ID"));

        assertEquals("../../../cocoon-profiling/ID", this.servletProfiler.createRelativeUrl(
                "http://localhost/myproject/foo/bar/buz/home.html?a=1", "ID"));

        assertEquals("../../../cocoon-profiling/ID", this.servletProfiler.createRelativeUrl(
                "http://localhost/myproject/foo/bar/buz/home.html?a=1&b=2", "ID"));

        assertEquals("../../../cocoon-profiling/ID", this.servletProfiler.createRelativeUrl(
                "http://localhost/myproject/foo/bar/buz/home.html?a=1&b=/", "ID"));
    }
}
