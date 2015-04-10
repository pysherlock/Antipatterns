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
package org.apache.cocoon.servlet.component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.component.AbstractPipelineComponent;
import org.apache.cocoon.pipeline.component.Finisher;
import org.apache.cocoon.pipeline.component.Starter;
import org.apache.cocoon.servlet.util.HttpContextHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RedirectorComponent extends AbstractPipelineComponent implements Starter, Finisher {

    private final Log logger = LogFactory.getLog(this.getClass());

    private Map<String, Object> parameters;
    private String uri;
    private URLConnection urlConnection;
    private OutputStream outputStream;

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.Starter#execute()
     */
    public void execute() {
        HttpServletResponse response = HttpContextHelper.getResponse(this.parameters);

        try {
            if (this.uri.startsWith("servlet:")) {
                InputStream inputStream = this.getURLConnection().getInputStream();
                byte[] data = new byte[1024];
                while (true) {
                    int bytesRead = inputStream.read(data, 0, data.length);

                    if (bytesRead == -1) {
                        break;
                    }

                    this.outputStream.write(data, 0, bytesRead);
                }
            } else {
                String location = response.encodeRedirectURL(this.uri);
                response.sendRedirect(location);
            }
        } catch (IOException e) {
            this.logger.error("Can't redirect to " + this.uri, e);
            throw new ProcessingException(e);
        }
    }

    private URLConnection getURLConnection() {
        if (this.urlConnection == null) {
            try {
                this.urlConnection = new URL(this.uri).openConnection();

            } catch (IOException e) {
                throw new ProcessingException("Can't connect to the URI " + this.uri, e);
            }
        }

        return this.urlConnection;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.Finisher#getContentType()
     */
    public String getContentType() {
        return this.getURLConnection().getContentType();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.PipelineComponent#setConfiguration(java.util.Map)
     */
    @Override
    public void setConfiguration(Map<String, ? extends Object> configuration) {
        this.uri = (String) configuration.get("uri");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.AbstractPipelineComponent#setup(java.util.Map)
     */
    @Override
    public void setup(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.component.Finisher#setOutputStream(java.io.OutputStream)
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "RedirectorComponent(" + this.uri + ")";
    }
}
