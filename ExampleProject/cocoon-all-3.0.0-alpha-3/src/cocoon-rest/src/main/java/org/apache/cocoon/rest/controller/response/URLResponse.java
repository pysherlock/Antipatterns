/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.rest.controller.response;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.pipeline.util.URLConnectionUtils;
import org.apache.cocoon.servlet.controller.ControllerContextHelper;
import org.apache.commons.io.IOUtils;

/**
 * A {@link URL} as controller response.
 */
public class URLResponse implements RestResponse {

    private Map<String, Object> data;

    private URLConnection servletConnection;

    private URL url;

    public URLResponse(String url) throws MalformedURLException {
        this.url = new URL(new URL("servlet:"), url);
        this.data = Collections.emptyMap();
    }

    public URLResponse(String url, Map<String, Object> data) throws MalformedURLException {
        this.url = new URL(new URL("servlet:"), url);
        this.data = data;
    }

    public URLResponse(URL url) {
        this.url = url;
        this.data = Collections.emptyMap();
    }

    public URLResponse(URL url, Map<String, Object> data) {
        this.url = url;
        this.data = data;
    }

    public void execute(OutputStream outputStream) throws Exception {
        IOUtils.copy(this.servletConnection.getInputStream(), outputStream);
    }

    public Map<String, Object> getData() {
        if (this.data == null) {
            this.data = new HashMap<String, Object>();
        }
        return this.data;
    }

    public URL getUrl() {
        return this.url;
    }

    public RestResponseMetaData setup(Map<String, Object> inputParameters)
            throws Exception {
        this.servletConnection = null;
        try {
            ControllerContextHelper.storeContext(this.data, inputParameters);

            this.servletConnection = this.url.openConnection();

            RestResponseMetaData restResponseMetaData = new RestResponseMetaData();

            if (this.servletConnection instanceof HttpURLConnection) {
                HttpURLConnection httpURLConnection = (HttpURLConnection) this.servletConnection;
                restResponseMetaData.setStatusCode(httpURLConnection.getResponseCode());
            }

            restResponseMetaData.setContentType(this.servletConnection.getContentType());
            return restResponseMetaData;
        } finally {
            URLConnectionUtils.closeQuietly(this.servletConnection);
        }
    }
}
