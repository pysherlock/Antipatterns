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
package org.apache.cocoon.rest.jaxrs.response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.cocoon.callstack.environment.CallFrameHelper;
import org.apache.cocoon.rest.jaxrs.container.CocoonJAXRSServlet;
import org.apache.cocoon.servlet.controller.ControllerContextHelper;
import org.apache.commons.io.IOUtils;

/**
 * A class to get a {@link ResponseBuilder} that has all information set correctly that is provided
 * by a {@link URLConnection}. It must be used when the JAX-RS container runs within the Cocoon
 * Servlet-Service framework.
 * 
 * @see CocoonJAXRSServlet
 */
public abstract class URLResponseBuilder extends ResponseBuilder {

    /**
     * The same as {@link #newInstance(URL)} but the URL is passed as {@link String}.
     */
    @SuppressWarnings("unchecked")
    public static ResponseBuilder newInstance(final String url) {
        return newInstance(url, Collections.EMPTY_MAP);
    }

    /**
     * The same as {@link #newInstance(URL, Map)} but the URL is passed as {@link String}.
     */
    public static ResponseBuilder newInstance(final String url, final Map<String, Object> data) {
        try {
            return newInstance(new URL(url), data);
        } catch (MalformedURLException e) {
            throw new WebApplicationException(500);
        }
    }

    /**
     * The same as {@link #newInstance(URL, Map)} but no objects are passed to the URL.
     */
    @SuppressWarnings("unchecked")
    public static ResponseBuilder newInstance(final URL url) {
        return newInstance(url, Collections.EMPTY_MAP);
    }

    /**
     * Create a {@link ResponseBuilder} that retrieves all available information (header parameters,
     * Last-Modified, Content-Type) from the {@link URLConnection} opened on the passed {@link URL}.
     * 
     * @param url The {@link URL} that provides the content for the entity.
     * @param data A {@link Map} of objects passed to the {@link URL}.
     * @return A {@link ResponseBuilder}
     */
    public static ResponseBuilder newInstance(final URL url, final Map<String, Object> data) {
        ResponseBuilder builder = ResponseBuilder.newInstance();
        URLConnection urlConnection = null;

        try {
            urlConnection = url.openConnection();
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;
                HttpServletRequest request = CallFrameHelper.getRequest();
                ControllerContextHelper.storeContext(data, request);

                // content type
                String contentType = httpUrlConnection.getContentType();
                if (contentType != null && !"".equals(contentType) && !"content/unknown".equals(contentType)) {
                    builder.type(contentType);
                }

                // headers
                Map<String, List<String>> headerFields = httpUrlConnection.getHeaderFields();
                for (Entry<String, List<String>> eachHeader : headerFields.entrySet()) {
                    List<String> headerValueList = eachHeader.getValue();
                    String value = null;
                    if (!headerValueList.isEmpty()) {
                        value = headerValueList.get(0);
                    }
                    builder.header(eachHeader.getKey(), value == null ? "" : value);
                }

                // status code
                int statusCode = httpUrlConnection.getResponseCode();
                builder.status(statusCode);

                // last modified
                long lastModified = urlConnection.getLastModified();
                if (lastModified >= 0) {
                    builder.lastModified(new Date(lastModified));
                }

                // entity
                builder.entity(IOUtils.toString(httpUrlConnection.getInputStream()));

                return builder;
            }
        } catch (IOException e) {
            throw new WebApplicationException(e, 500);
        }

        throw new WebApplicationException(500);
    }
}
