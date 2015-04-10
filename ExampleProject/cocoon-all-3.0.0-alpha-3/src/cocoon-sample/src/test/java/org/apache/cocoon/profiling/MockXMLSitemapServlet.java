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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.servlet.collector.ResponseHeaderCollector;
import org.apache.cocoon.servlet.util.HttpContextHelper;
import org.apache.cocoon.sitemap.Invocation;

class MockXMLSitemapServlet extends HttpServlet implements SitemapServlet {

    private static final long serialVersionUID = 1L;

    private ServletConfig servletConfig;
    private String version = "3";
    private ProfilingIntegrationTest test;

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.servletConfig = config;
        super.init(this.servletConfig);
    }

    public void invoke(String requestURI, Map<String, Object> parameters, OutputStream outputStream)
            throws ServletException {

        Invocation buildInvocation = this.test.buildInvocation(requestURI, parameters);

        this.test.sitemap.invoke(buildInvocation);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {

        try {
            Map<String, Object> parameters = this.getInvocationParameters(request);
            HttpContextHelper.storeRequest(request, parameters);
            HttpContextHelper.storeResponse(response, parameters);

            // invoke the sitemap engine
            ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
            this.invoke(this.calcSitemapRequestURI(request), parameters, baos);

            // collect meta information from the previous run of the sitemap
            // engine
            long lastModified = ResponseHeaderCollector.getLastModified();
            int contentLengh = baos.size();

            // send the Last-Modified header
            if (lastModified > -1) {
                response.setDateHeader("Last-Modified", lastModified);
            }

            response.setHeader("X-Cocoon-Version", this.version);

            // Content-Type handling
            String mimeType = ResponseHeaderCollector.getMimeType();
            if (mimeType == null || "".equals(mimeType) || "content/unknown".equals(mimeType)) {
                mimeType = this.servletConfig.getServletContext().getMimeType(request.getRequestURI());
            }
            if (mimeType != null) {
                response.setContentType(mimeType);
            }

            // Status code handling
            int statusCode = HttpServletResponse.SC_OK;
            response.setStatus(statusCode);

            // write the sitemap result to the output stream
            if (contentLengh > 0) {
                response.setContentLength(contentLengh);
                response.getOutputStream().write(baos.toByteArray());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String calcSitemapRequestURI(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String mountPath = request.getServletPath();
        return request.getRequestURI().substring(contextPath.length() + mountPath.length());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getInvocationParameters(HttpServletRequest req) {
        Map<String, Object> invocationParameters = new HashMap<String, Object>();

        for (Enumeration<String> names = req.getParameterNames(); names.hasMoreElements();) {
            String name = names.nextElement();
            invocationParameters.put(name, req.getParameter(name));
        }

        return invocationParameters;
    }

    public void setTestcase(ProfilingIntegrationTest test) {
        this.test = test;
    }
}
