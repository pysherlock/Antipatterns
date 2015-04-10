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
package org.apache.cocoon.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Cocoon 3 servlet. It works with and without the Servlet-Service framework.
 */
public class XMLSitemapServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private RequestProcessor requestProcessor;
    private final Log logger = LogFactory.getLog(this.getClass());

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.lazyInitialize();

        try {
            this.requestProcessor.service(req, resp);
        } catch (RuntimeException e) {
            this.wrapException(e, "Cocoon can't process the request.");
        }
    }

    private void lazyInitialize() throws ServletException {
        synchronized (this) {
            if (this.requestProcessor != null) {
                return;
            }

            try {
                this.requestProcessor = new RequestProcessor(this.getServletContext(), this
                        .getInitParameter("sitemap-path"), WebAppContextUtils.getCurrentWebApplicationContext());
            } catch (Exception e) {
                this.wrapException(e, "Can't initialize the RequestProcessor correctly.");
            }
        }
    }

    private ServletException wrapException(Exception e, String msg) {
        this.logger.error(msg, e);

        ServletException servletException = new ServletException(msg, e);
        if (servletException.getCause() == null) {
            servletException.initCause(e);
        }

        return servletException;
    }
}
