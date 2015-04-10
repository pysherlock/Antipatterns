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
package org.apache.cocoon.sitemap;

import java.io.OutputStream;
import java.net.URL;
import java.util.Map;

public interface Invocation {

    void execute() throws Exception;

    OutputStream getOutputStream();

    Object getParameter(String name);

    Map<String, Object> getParameters();

    String getRequestURI();

    Throwable getThrowable();

    boolean hasCompletePipeline();

    void installAction(String type);

    void installComponent(String type, Map<String, ? extends Object> parameters);

    void installPipeline(String type, Map<String, ? extends Object> parameters);

    boolean isErrorInvocation();

    void reset();

    URL resolve(String resource);

    void setOutputStream(OutputStream outputStream);

    void setParameters(Map<String, Object> parameters);

    void setThrowable(Throwable throwable);

    void pushSitemapParameters(String nodeName, Map<String, ? extends Object> sitemapParameters);

    void popSitemapParameters();

    String resolveParameter(final String parameter);
}
