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
package org.apache.cocoon.servlet.util;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.cocoon.servletservice.AbsoluteServletConnection;
import org.apache.cocoon.servletservice.Absolutizable;

public class ServletServiceUtils {

    /**
     * Get an {@link InputStream} of a Servlet that is a servlet-service as defined by the Cocoon Servlet-Service
     * framework.
     * 
     * @param servlet The Servlet-Service reference.
     * @param resourcePath The absolute resource path.
     * @return An InputStream of the resource.
     * @throws ServletServiceException If any problem occurs, this unchecked exception is thrown.
     */
    public static InputStream getServletServiceResource(Servlet servlet, String resourcePath) {
        return getServletServiceResource(servlet, resourcePath, null);
    }

    /**
     * Get an {@link InputStream} of a Servlet that is a servlet-service as defined by the Cocoon Servlet-Service
     * framework.
     * 
     * @param servlet The Servlet-Service reference.
     * @param resourcePath The absolute resource path.
     * @param query The query string.
     * @return An InputStream of the resource.
     * @throws ServletServiceException If any problem occurs, this unchecked exception is thrown.
     */
    public static InputStream getServletServiceResource(Servlet servlet, String resourcePath, String query) {
        String resultResourcePath = resourcePath;
        if (resultResourcePath.length() > 0 && !resultResourcePath.startsWith("/")) {
            resultResourcePath = "/" + resultResourcePath;
        }

        Absolutizable a;
        try {
            a = (Absolutizable) servlet.getServletConfig().getServletContext();
        } catch (ClassCastException cce) {
            throw new ServletServiceException("The passed servlet isn't a servlet service because it can't be cast to "
                    + Absolutizable.class.getName() + ".", cce);
        }

        AbsoluteServletConnection sc = new AbsoluteServletConnection(a.getServiceName(), resultResourcePath, query);
        try {
            return sc.getInputStream();
        } catch (IOException e) {
            throw new ServletServiceException(e);
        } catch (ServletException e) {
            throw new ServletServiceException(e);
        }
    }

    /**
     * A general purpose {@link RuntimeException} that is used to indicate any problem with accessing a servlet-service.
     */
    public static class ServletServiceException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public ServletServiceException(String message, Throwable t) {
            super(message, t);
        }

        public ServletServiceException(Throwable t) {
            super(t);
        }
    }
}
