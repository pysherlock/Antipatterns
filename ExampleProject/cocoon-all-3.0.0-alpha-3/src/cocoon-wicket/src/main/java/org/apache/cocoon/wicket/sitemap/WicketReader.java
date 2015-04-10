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
package org.apache.cocoon.wicket.sitemap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.servlet.collector.ResponseHeaderCollector;
import org.apache.cocoon.servlet.util.HttpContextHelper;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.spring.SpringWebApplicationFactory;

/**
 * The simplest possible integration of Wicket as a reader component.
 */
public class WicketReader implements Starter, Finisher {

    private static final String WICKET_FILTER_NAME = "wicket.filter";

    private static WicketFilter wicketFilter;

    private String basePath;
    private OutputStream cocoonOutputStream;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;

    public void execute() {
        if ("GET".equalsIgnoreCase(this.request.getMethod()) || "POST".equalsIgnoreCase(this.request.getMethod())) {
            try {
                wicketFilter.doGet(this.request, this.response);
            } catch (ServletException e) {
                throw new ProcessingException(e);
            } catch (IOException e) {
                throw new ProcessingException(e);
            }
        }
    }

    public void finish() {
        // nothing to do
    }

    public String getContentType() {
        // no relevance because this is handled by Wicket
        return null;
    }

    public void setConfiguration(Map<String, ? extends Object> configuration) {
        String basePath = (String) configuration.get("base-path");
        if (basePath == null || "".equals(basePath)) {
            throw new SetupException("The parameter 'base-path' has to be set. "
                    + "This is the path where Wicket will be mounted to your URI space.");
        }
        if (basePath.charAt(0) != '/') {
            basePath = "/" + basePath;
        }
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        this.basePath = basePath;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.cocoonOutputStream = outputStream;
        this.response = new CocoonWicketHttpServletResponse(this.response, this.cocoonOutputStream);
    }

    public void setup(Map<String, Object> parameters) {
        this.request = HttpContextHelper.getRequest(parameters);
        this.response = HttpContextHelper.getResponse(parameters);
        this.servletContext = new CocoonWicketServletContext(HttpContextHelper.getServletContext(parameters),
                this.basePath);
        this.initWicketFilter();
    }

    private synchronized void initWicketFilter() {
        if (wicketFilter != null) {
            return;
        }

        wicketFilter = new WicketFilter();
        try {
            wicketFilter.init(new FilterConfig() {

                public String getFilterName() {
                    return WICKET_FILTER_NAME;
                }

                public String getInitParameter(String name) {
                    if ("applicationFactoryClassName".equals(name)) {
                        return SpringWebApplicationFactory.class.getName();
                    }
                    return null;
                }

                public Enumeration<String> getInitParameterNames() {
                    Vector<String> paramNames = new Vector<String>();
                    paramNames.add("applicationFactoryClassName");
                    return paramNames.elements();
                }

                public ServletContext getServletContext() {
                    return WicketReader.this.servletContext;
                }
            });
        } catch (ServletException e) {
            throw new SetupException("Can't initialize Wicket.", e);
        }
    }

    @SuppressWarnings( { "deprecation" })
    private static class CocoonWicketHttpServletResponse implements HttpServletResponse {

        private final OutputStream cocoonOutputStream;
        private PrintWriter printWriter;
        private final HttpServletResponse response;

        public CocoonWicketHttpServletResponse(HttpServletResponse response, OutputStream cocoonOutputStream) {
            this.response = response;
            this.cocoonOutputStream = cocoonOutputStream;
        }

        public void addCookie(Cookie cookie) {
            this.response.addCookie(cookie);
        }

        public void addDateHeader(String name, long date) {
            this.response.addDateHeader(name, date);
        }

        public void addHeader(String name, String value) {
            this.response.addHeader(name, value);
        }

        public void addIntHeader(String name, int value) {
            this.response.addIntHeader(name, value);
        }

        public boolean containsHeader(String name) {
            return this.response.containsHeader(name);
        }

        public String encodeRedirectUrl(String url) {
            return this.response.encodeRedirectUrl(url);
        }

        public String encodeRedirectURL(String url) {
            return this.response.encodeRedirectURL(url);
        }

        public String encodeUrl(String url) {
            return this.response.encodeUrl(url);
        }

        public String encodeURL(String url) {
            return this.response.encodeURL(url);
        }

        public void flushBuffer() throws IOException {
            this.response.flushBuffer();
        }

        public int getBufferSize() {
            return this.response.getBufferSize();
        }

        public String getCharacterEncoding() {
            return this.response.getCharacterEncoding();
        }

        public String getContentType() {
            return this.response.getContentType();
        }

        public Locale getLocale() {
            return this.response.getLocale();
        }

        public ServletOutputStream getOutputStream() throws IOException {
            return new ServletOutputStream() {

                @Override
                public void write(int b) throws IOException {
                    CocoonWicketHttpServletResponse.this.cocoonOutputStream.write(b);
                }
            };
        }

        public PrintWriter getWriter() throws IOException {
            if (this.printWriter == null) {
                this.printWriter = new PrintWriter(new OutputStreamWriter(this.cocoonOutputStream), true) {
                    @Override
                    public PrintWriter append(char c) {
                        return super.append(c);
                    }

                    @Override
                    public PrintWriter append(CharSequence csq) {
                        return super.append(csq);
                    }

                    @Override
                    public void write(char[] buf, int off, int len) {
                        super.write(buf, off, len);
                        // flush the print-writer because otherwise it never happens at all
                        super.flush();
                    }
                };
            }

            return this.printWriter;
        }

        public boolean isCommitted() {
            return this.response.isCommitted();
        }

        public void reset() {
            this.response.reset();
        }

        public void resetBuffer() {
            this.response.resetBuffer();
        }

        public void sendError(int sc) throws IOException {
            this.response.sendError(sc);
        }

        public void sendError(int sc, String msg) throws IOException {
            this.response.sendError(sc, msg);
        }

        public void sendRedirect(String location) throws IOException {
            this.response.sendRedirect(location);
        }

        public void setBufferSize(int size) {
            this.response.setBufferSize(size);
        }

        public void setCharacterEncoding(String charset) {
            this.response.setCharacterEncoding(charset);
        }

        public void setContentLength(int len) {
            this.response.setContentLength(len);
        }

        public void setContentType(String type) {
            this.response.setContentType(type);
        }

        public void setDateHeader(String name, long date) {
            if ("Last-Modified".equals(name)) {
                ResponseHeaderCollector.setLastModified(date);
            }
            this.response.setDateHeader(name, date);
        }

        public void setHeader(String name, String value) {
            this.response.setHeader(name, value);
        }

        public void setIntHeader(String name, int value) {
            this.response.setIntHeader(name, value);
        }

        public void setLocale(Locale loc) {
            this.response.setLocale(loc);
        }

        public void setStatus(int sc) {
            this.response.setStatus(sc);
        }

        public void setStatus(int sc, String sm) {
            this.response.setStatus(sc, sm);
        }
    }

    @SuppressWarnings( { "unchecked", "deprecation" })
    private static class CocoonWicketServletContext implements ServletContext {

        private final String basePath;
        private final ServletContext servletContext;

        public CocoonWicketServletContext(ServletContext servletContext, String basPath) {
            this.servletContext = servletContext;
            this.basePath = basPath;
        }

        public Object getAttribute(String name) {
            return this.servletContext.getAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return this.servletContext.getAttributeNames();
        }

        public ServletContext getContext(String uripath) {
            return this.servletContext.getContext(uripath);
        }

        public String getInitParameter(String name) {
            return this.servletContext.getInitParameter(name);
        }

        public Enumeration getInitParameterNames() {
            return this.servletContext.getInitParameterNames();
        }

        public int getMajorVersion() {
            return this.servletContext.getMajorVersion();
        }

        public String getMimeType(String file) {
            return this.servletContext.getMimeType(file);
        }

        public int getMinorVersion() {
            return this.servletContext.getMinorVersion();
        }

        public RequestDispatcher getNamedDispatcher(String name) {
            return this.servletContext.getNamedDispatcher(name);
        }

        public String getRealPath(String path) {
            return this.servletContext.getRealPath(path);
        }

        public RequestDispatcher getRequestDispatcher(String path) {
            return this.servletContext.getRequestDispatcher(path);
        }

        public URL getResource(String path) throws MalformedURLException {
            return this.servletContext.getResource(path);
        }

        public InputStream getResourceAsStream(String path) {
            if ("/WEB-INF/web.xml".equals(path)) {
                String filterDefinition = "<web-app><filter-mapping><filter-name>" + WICKET_FILTER_NAME
                        + "</filter-name><url-pattern>" + this.basePath + "/*</url-pattern></filter-mapping></web-app>";
                return IOUtils.toInputStream(filterDefinition);
            }
            return this.servletContext.getResourceAsStream(path);
        }

        public Set getResourcePaths(String path) {
            return this.servletContext.getResourcePaths(path);
        }

        public String getServerInfo() {
            return this.servletContext.getServerInfo();
        }

        public Servlet getServlet(String name) throws ServletException {
            return this.servletContext.getServlet(name);
        }

        public String getServletContextName() {
            return this.servletContext.getServletContextName();
        }

        public Enumeration getServletNames() {
            return this.servletContext.getServletNames();
        }

        public Enumeration getServlets() {
            return this.servletContext.getServlets();
        }

        public void log(Exception exception, String msg) {
            this.servletContext.log(exception, msg);
        }

        public void log(String msg) {
            this.servletContext.log(msg);
        }

        public void log(String message, Throwable throwable) {
            this.servletContext.log(message, throwable);
        }

        public void removeAttribute(String name) {
            this.servletContext.removeAttribute(name);
        }

        public void setAttribute(String name, Object object) {
            this.servletContext.setAttribute(name, object);
        }
    }
}
