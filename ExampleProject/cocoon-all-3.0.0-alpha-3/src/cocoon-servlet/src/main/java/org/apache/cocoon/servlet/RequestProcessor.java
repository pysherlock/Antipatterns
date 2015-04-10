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
/**
 *
 */
package org.apache.cocoon.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.callstack.CallStack;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.servlet.collector.ResponseHeaderCollector;
import org.apache.cocoon.servlet.util.HttpContextHelper;
import org.apache.cocoon.servlet.util.ManifestUtils;
import org.apache.cocoon.servlet.util.ObjectModelProvider;
import org.apache.cocoon.servlet.util.SettingsHelper;
import org.apache.cocoon.servletservice.CallStackHelper;
import org.apache.cocoon.sitemap.Invocation;
import org.apache.cocoon.sitemap.InvocationImpl;
import org.apache.cocoon.sitemap.SitemapBuilder;
import org.apache.cocoon.sitemap.node.SitemapNode;
import org.apache.cocoon.spring.configurator.ResourceUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;

public class RequestProcessor {

    private URL baseURL;
    private BeanFactory beanFactory;
    private boolean inServletServiceFramework;
    private final Log logger = LogFactory.getLog(this.getClass());
    private ServletContext servletContext;
    private SitemapNode sitemapNode;
    private String sitemapPath;
    private String version = "";

    public RequestProcessor(ServletContext servletContext, String sitemapPath, BeanFactory beanFactory)
            throws SitemapNotFoundException, InvalidBaseUrlException, SitemapInitializationException {
        if (servletContext == null) {
            throw new NullPointerException("A 'ServletContext' has to be passed.");
        }
        if (beanFactory == null) {
            throw new NullPointerException("A Spring 'BeanFactory' has to be passed.");
        }

        this.servletContext = servletContext;
        this.sitemapPath = sitemapPath;
        this.beanFactory = beanFactory;

        this.initializeInServletServiceFramework();
        this.initializeBaseURL();
        this.initializeVersionNumber();
        this.initializeSitemap();
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        long start = System.nanoTime();
        this.logRequest(request);

        try {
            if (this.inServletServiceFramework) {
                this.sendSitemapResponse(request, response);
                return;
            }

            // if it runs outside of the Servlet-Service framework, the CallStack has
            // to be prepared
            try {
                CallStackHelper.enterServlet(this.servletContext, request, response);
                this.sendSitemapResponse(request, response);
            } finally {
                CallStackHelper.leaveServlet();
            }
        } finally {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Sitemap execution for " + request.getRequestURI() + " took "
                        + (System.nanoTime() - start) / 1000000f + " ms.");
            }
        }
    }

    private String calcSitemapRequestURI(HttpServletRequest request) {
        if (!this.inServletServiceFramework) {
            return request.getServletPath();
        }

        // the Servlet-Service framework uses the servlet path as mount
        // path of a servlet
        String contextPath = request.getContextPath();
        String mountPath = request.getServletPath();
        return request.getRequestURI().substring(contextPath.length() + mountPath.length());
    }

    private URL getBaseURL() {
        return this.baseURL;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getInvocationParameters(HttpServletRequest req) {
        Map<String, Object> invocationParameters = new HashMap<String, Object>();

        for (Enumeration<String> names = req.getParameterNames(); names.hasMoreElements();) {
            String name = names.nextElement();
            invocationParameters.put(name, req.getParameter(name));
        }

        return invocationParameters;
    }

    private String getSitemapPath() {
        String sitemapPath = this.sitemapPath;
        if (sitemapPath == null) {
            sitemapPath = "/sitemap.xmap";
        }

        if (!sitemapPath.startsWith("/")) {
            sitemapPath = "/" + sitemapPath;
        }

        return sitemapPath;
    }

    private void initializeBaseURL() throws InvalidBaseUrlException {
        try {
            String baseURL = this.getSitemapPath();
            baseURL = baseURL.substring(0, baseURL.lastIndexOf('/') + 1);

            this.baseURL = this.servletContext.getResource(baseURL);

            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Setting the baseURL to " + this.baseURL);
            }
        } catch (MalformedURLException e) {
            throw new InvalidBaseUrlException("An exception occurred while retrieving the base "
                    + "URL from the servlet context.", e);
        }
    }

    private void initializeInServletServiceFramework() {
        this.inServletServiceFramework = CallStack.getCurrentFrame() != null;
    }

    private void initializeSitemap() throws SitemapNotFoundException, SitemapInitializationException {
        URL url = null;
        SitemapBuilder sitemapBuilder = null;
        try {
            sitemapBuilder = (SitemapBuilder) this.beanFactory.getBean(SitemapBuilder.class.getName());
            url = this.servletContext.getResource(this.getSitemapPath());
        } catch (Exception e) {
            throw new SitemapInitializationException("Can't initialize sitemap.", e);
        }

        // if the sitemap URL can't be resolved by the ServletContext, null is returned
        if (url == null) {
            // prepare a meaningful exception
            String baseURL = this.getBaseURL().toExternalForm();
            if (baseURL.endsWith("/")) {
                baseURL = baseURL.substring(0, baseURL.length() - 1);
            }
            throw new SitemapNotFoundException("Can't find sitemap at " + baseURL + this.getSitemapPath());
        }

        this.sitemapNode = sitemapBuilder.build(url);
    }

    /**
     * Read versioning information from the Cocoon 3 Servlet module
     */
    private void initializeVersionNumber() {
        Properties pomProperties = ResourceUtils.getPOMProperties("org.apache.cocoon.servlet", "cocoon-servlet");
        if (pomProperties == null) {
            return;
        }

        String servletModuleVersion = pomProperties.getProperty("version");
        if (servletModuleVersion != null) {
            this.version = servletModuleVersion;
        }

        if (this.version.endsWith("SNAPSHOT")) {
            String buildNumber = "";
            try {
                String buildNumberAttr = ManifestUtils.getAttribute(this.getClass(), "Implementation-Build");
                if (buildNumberAttr != null && !"".equals(buildNumberAttr) && !"na".equals(buildNumberAttr)) {
                    buildNumber = "/rev" + buildNumberAttr;
                }
            } catch (IOException e) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Error while reading an attribute from the manifest.", e);
                }
            }
            this.version += buildNumber;
        }
    }

    private void invoke(String requestURI, Map<String, Object> parameters, OutputStream outputStream) {
        InvocationImpl invocation = (InvocationImpl) this.beanFactory.getBean(Invocation.class.getName());

        invocation.setBaseURL(this.getBaseURL());
        invocation.setRequestURI(requestURI);
        invocation.setParameters(parameters);
        invocation.setOutputStream(outputStream);
        invocation.setObjectModel(ObjectModelProvider.provide(parameters));

        this.sitemapNode.invoke(invocation);
    }

    private void logRequest(HttpServletRequest request) {
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Performing " + request.getMethod().toUpperCase() + " request at "
                    + request.getRequestURI());
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("The base URL for this request is " + this.getBaseURL());
        }
    }

    public static Map<String, Object> prepareParameters(HttpServletRequest request, HttpServletResponse response,
            Settings settings, ServletContext servletContext) {
        if (request == null) {
            throw new NullPointerException("Request mustn't be null.");
        }
        if (response == null) {
            throw new NullPointerException("Response mustn't be null.");
        }
        if (servletContext == null) {
            throw new NullPointerException("ServletContext mustn't be null.");
        }

        Map<String, Object> parameters = getInvocationParameters(request);

        HttpContextHelper.storeRequest(request, parameters);
        HttpContextHelper.storeResponse(response, parameters);
        HttpContextHelper.storeServletContext(servletContext, parameters);

        if (settings != null) {
            SettingsHelper.storeSettings(settings, parameters);
        }

        return parameters;
    }

    private void sendSitemapResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Settings settings = (Settings) this.beanFactory.getBean(Settings.class.getName());

        // provide conditional GET relevant data and request method
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // read the 'If-Modified-Since' request header
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        ResponseHeaderCollector.setIfLastModifiedSince(ifModifiedSince);

        // read the 'If-None-Match' request header
        String ifNoneMatch = request.getHeader("If-None-Match");
        ResponseHeaderCollector.setIfNoneMatch(ifNoneMatch);

        // request method
        ResponseHeaderCollector.setRequestMethod(request.getMethod());

        // invoke the sitemap engine
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
        this.invoke(this.calcSitemapRequestURI(request), prepareParameters(request, response, settings,
                this.servletContext), baos);

        // read data after sitemap/pipeline execution
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        // read the produced status code
        int statusCode = ResponseHeaderCollector.getStatusCode();

        // collect meta information from the previous exeuction of the sitemap engine
        long lastModified = ResponseHeaderCollector.getLastModified();
        String etag = ResponseHeaderCollector.getETag();

        // set response headers
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        if (statusCode >= 200 && statusCode < 300) {
            // send the ETag header
            if (etag != null && !"".equals(etag)) {
                response.setHeader("ETag", etag);
            }
            // send the Last-Modified
            if (lastModified > -1) {
                response.setDateHeader("Last-Modified", lastModified);
            }
        }

        // set the X-Cocoon-Version header
        if (!"false".equals(settings.getProperty("org.apache.cocoon.show-version"))) {
            response.setHeader("X-Cocoon-Version", this.version);
        }

        // Content-Type handling
        String mimeType = ResponseHeaderCollector.getMimeType();
        if (mimeType == null || "".equals(mimeType) || "content/unknown".equals(mimeType)) {
            mimeType = this.servletContext.getMimeType(request.getRequestURI());
        }
        if (mimeType != null) {
            response.setContentType(mimeType);
        }

        // conditional request support (no need to send an unmodified response)
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        if (!ResponseHeaderCollector.isModifiedResponse()) {
            if (this.logger.isInfoEnabled()) {
                this.logger.info("Going to send NOT MODIFIED response: statusCode="
                        + HttpServletResponse.SC_NOT_MODIFIED + ", lastModified="
                        + lastModified);
            }

            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        // write the sitemap result to the output stream if at least one byte is available and
        // it is not a HEAD request. The Content-Length has to be sent also in the case of
        // a HEAD request (http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html).
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        int contentLengh = baos.size();
        response.setContentLength(contentLengh);

        // Status code handling
        response.setStatus(statusCode);

        // logging
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Going to send " + request.getMethod().toUpperCase() + " response: mimeType=" + mimeType
                    + ", contentLength=" + contentLengh + ", statusCode=" + statusCode + ", lastModified="
                    + lastModified);
        }

        // in the case of a HEAD request stop processing here (i.e. don't send any content)
        if ("HEAD".equalsIgnoreCase(request.getMethod())) {
            return;
        }

        // send content
        if (contentLengh > 0) {
            response.getOutputStream().write(baos.toByteArray());
        }
    }

    public static class InvalidBaseUrlException extends RequestProcessorException {

        private static final long serialVersionUID = 1L;

        public InvalidBaseUrlException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class RequestProcessorException extends Exception {

        private static final long serialVersionUID = 1L;

        public RequestProcessorException(String message) {
            super(message);
        }

        public RequestProcessorException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class SitemapInitializationException extends RequestProcessorException {

        private static final long serialVersionUID = 1L;

        public SitemapInitializationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class SitemapNotFoundException extends RequestProcessorException {

        private static final long serialVersionUID = 1L;

        public SitemapNotFoundException(String message) {
            super(message);
        }
    }
}