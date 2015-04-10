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
package org.apache.cocoon.servlet.ssf;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.SetupException;
import org.apache.cocoon.pipeline.util.URLConnectionUtils;
import org.apache.cocoon.sax.AbstractSAXSerializer;
import org.apache.cocoon.sax.util.XMLUtils;
import org.apache.cocoon.servlet.collector.ResponseHeaderCollector;
import org.apache.cocoon.xml.sax.SAXBuffer;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

public class ServletServiceSerializer extends AbstractSAXSerializer {

    private URL service;
    private SAXBuffer saxBuffer = new SAXBuffer();
    private URLConnection servletConnection;

    @Override
    public void endDocument() throws SAXException {
        if (this.service == null) {
            throw new IllegalArgumentException("ServletServiceSerializer has no service set.");
        }

        this.saxBuffer.endDocument();

        try {
            XMLUtils.toOutputStream(this.getUrlConnection().getOutputStream(), this.saxBuffer);
            IOUtils.copy(this.getUrlConnection().getInputStream(), this.getOutputStream());

            // set the status code
            URLConnection urlConnection = this.getUrlConnection();
            if (urlConnection instanceof HttpURLConnection) {
                HttpURLConnection servletURLConnection = (HttpURLConnection) urlConnection;
                int statusCode = servletURLConnection.getResponseCode();
                ResponseHeaderCollector.setStatusCode(statusCode);
            }
        } catch (IOException e) {
            throw new SAXException("Can't copy the result of the servlet service to the output stream", e);
        } finally {
            URLConnectionUtils.closeQuietly(this.getUrlConnection());
        }
    }

    @Override
    public String getContentType() {
        return this.getUrlConnection().getContentType();
    }

    public void init() {
        this.setContentHandler(this.saxBuffer);
    }

    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        try {
            this.service = new URL((String) configuration.get("service"));
        } catch (MalformedURLException e) {
            throw new SetupException(("Can't create an URL for " + configuration.get("service") + "."), e);
        }
    }

    private URLConnection getUrlConnection() {
        if (this.servletConnection == null) {
            try {
                this.servletConnection = this.service.openConnection();
            } catch (IOException e) {
                throw new ProcessingException("Can't use connected servlet service: " + this.service, e);
            }
        }

        return this.servletConnection;
    }
}