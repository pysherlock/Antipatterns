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
package org.apache.cocoon.wicket.target;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.cocoon.servlet.RequestProcessor;
import org.apache.cocoon.servlet.RequestProcessor.InvalidBaseUrlException;
import org.apache.cocoon.servlet.RequestProcessor.SitemapInitializationException;
import org.apache.cocoon.servlet.RequestProcessor.SitemapNotFoundException;
import org.apache.cocoon.sitemap.NoMatchingPipelineException;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.request.WebErrorCodeResponseTarget;
import org.apache.wicket.settings.IExceptionSettings;

public class CocoonSitemapRequestTarget implements IRequestTarget {

    private RequestProcessor requestProcessor;
    private final String mountPath;

    public CocoonSitemapRequestTarget(final String sitemapPath, String mountPath) {
        this.mountPath = mountPath;
        synchronized (this) {
            if (this.requestProcessor != null) {
                return;
            }

            try {
                this.requestProcessor = new RequestProcessor(WebApplication.get().getServletContext(), sitemapPath,
                        WebAppContextUtils.getCurrentWebApplicationContext());
            } catch (SitemapNotFoundException e) {
                throw new CocoonRuntimeException("Can't initialize Cocoon sitemap.", e);
            } catch (InvalidBaseUrlException e) {
                throw new CocoonRuntimeException("Invalid base URL for the Cocoon sitemap.", e);
            } catch (SitemapInitializationException e) {
                throw new CocoonRuntimeException("Can't initialize Cocoon sitemap.", e);
            }
        }
    }

    public void detach(RequestCycle requestCycle) {
    }

    public void respond(RequestCycle requestCycle) {
        try {
            HttpServletRequest request = new CocoonWicketRequestWrapper(((WebRequest) requestCycle.getRequest())
                    .getHttpServletRequest());
            this.requestProcessor.service(request,
                    ((WebResponse) requestCycle.getResponse()).getHttpServletResponse());
        } catch (NoMatchingPipelineException e) {
            WebApplication.get().getExceptionSettings().setUnexpectedExceptionDisplay(
                    IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
            RequestCycle.get().setRequestTarget(new WebErrorCodeResponseTarget(404));
        } catch (Exception e) {
            throw new CocoonRuntimeException("Error occurred while executing a Cocoon sitemap.", e);
        }
    }

    public static class CocoonRuntimeException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public CocoonRuntimeException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    private class CocoonWicketRequestWrapper extends HttpServletRequestWrapper {

        private final HttpServletRequest request;

        public CocoonWicketRequestWrapper(HttpServletRequest request) {
            super(request);
            this.request = request;
        }

        @Override
        public String getServletPath() {
            return this.request.getServletPath().substring(
                    CocoonSitemapRequestTarget.this.mountPath.length());
        }
    }
}
