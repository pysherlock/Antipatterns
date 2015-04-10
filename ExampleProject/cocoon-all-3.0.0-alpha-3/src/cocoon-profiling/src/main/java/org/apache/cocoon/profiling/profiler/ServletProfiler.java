/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.profiling.profiler;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.profiling.ProfileMethod;
import org.apache.cocoon.profiling.ProfileMethodType;
import org.apache.cocoon.profiling.data.ProfilingData;

/**
 * The profiler for {@link Servlet}, which intercepts the service method to add the profiling id
 * information to the HTTP header.
 */
public class ServletProfiler extends Profiler<Servlet> {

    private final static String PROFILING_CONTROLLER_PATH = "cocoon-profiling/";
    private final static String PROFILING_ID_HEADER = "X-Cocoon-Profiling-ID";
    private final static String PROFILING_URL_HEADER = "X-Cocoon-Profiling-URL";
    private String mountPath;

    public ServletProfiler() {
        this(Servlet.class);
    }

    protected ServletProfiler(Class<? extends Servlet> clazz) {
        super(clazz);
        this.setMountPath("/");// TODO
    }

    @ProfileMethod(name = "service", type = ProfileMethodType.BEFORE_INVOCATION)
    public final void beforeService(ProfilingData data, Servlet component, Object[] args) {
        if (data.isRoot()) {
            HttpServletRequest request = (HttpServletRequest) args[0];
            HttpServletResponse response = (HttpServletResponse) args[1];
            String profilingId = data.getProfilingId();
            String requestUrl = request.getRequestURL().toString();
            response.addHeader(PROFILING_ID_HEADER, profilingId);
            response.addHeader(PROFILING_URL_HEADER, this.createRelativeUrl(requestUrl, profilingId));
        }

        HttpServletRequest request = (HttpServletRequest) args[0];
        String requestURI = request.getRequestURI();
        String className = component.getClass().getSimpleName();

        data.setDisplayName(String.format("%s (request=%s)", className, requestURI));
    }

    public String createRelativeUrl(String inputUrl, String profilingId) {
        String mountPath = this.getMountPath();

        int questionMarkIndex = inputUrl.indexOf('?');
        if (questionMarkIndex != -1) {
            // ignore parameters (could contain slashes)
            inputUrl = inputUrl.substring(0, questionMarkIndex);
        }

        // Don't count the slashes of http://
        int inputUrlSlashes = this.countOccurenceOfCharacter('/', inputUrl) - 2;
        int mountPathSlashes = this.countOccurenceOfCharacter('/', mountPath);

        StringBuilder sb = new StringBuilder();

        for (int i = mountPathSlashes; i < inputUrlSlashes; i++) {
            sb.append("../");
        }

        sb.append(PROFILING_CONTROLLER_PATH);
        sb.append(profilingId);

        return sb.toString();
    }

    public String getMountPath() {
        return this.mountPath;
    }

    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    private int countOccurenceOfCharacter(char c, String s) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }
}
