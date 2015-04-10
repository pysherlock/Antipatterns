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
package org.apache.cocoon.sitemap.component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.cocoon.pipeline.ProcessingException;
import org.apache.cocoon.pipeline.caching.CacheKey;
import org.apache.cocoon.pipeline.caching.TimestampCacheKey;
import org.apache.cocoon.pipeline.component.CachingPipelineComponent;
import org.apache.cocoon.pipeline.util.URLConnectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileReaderComponent extends AbstractReader implements CachingPipelineComponent {

    private final Log logger = LogFactory.getLog(this.getClass());

    public FileReaderComponent() {
        super();
    }

    public FileReaderComponent(URL source) {
        super(source);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.CachingPipelineComponent#constructCacheKey()
     */
    public CacheKey constructCacheKey() {
        try {
            URLConnection connection = this.source.openConnection();
            TimestampCacheKey timestampCacheKey = new TimestampCacheKey(this.source, connection.getLastModified());
            URLConnectionUtils.closeQuietly(connection);
            return timestampCacheKey;
        } catch (IOException e) {
            this.logger.error("Can't construct cache key. Error while connecting to " + this.source, e);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.Starter#execute()
     */
    public void execute() {
        InputStream inputStream = null;
        if (this.source == null) {
            throw new IllegalArgumentException("FileReaderComponent has no source configured to read from.");
        }
        URLConnection connection = null;
        try {
            connection = this.source.openConnection();
            inputStream = connection.getInputStream();

            byte[] data = new byte[1024];
            while (true) {
                int bytesRead = inputStream.read(data, 0, data.length);

                if (bytesRead == -1) {
                    break;
                }

                this.outputStream.write(data, 0, bytesRead);
            }

        } catch (IOException e) {
            String message = "FileReader cannot read from '" + this.source + "'";
            this.logger.error(message, e);
            throw new ProcessingException(message, e);
        } finally {
            URLConnectionUtils.closeQuietly(connection);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.cocoon.pipeline.component.Finisher#getContentType()
     */
    public String getContentType() {
        if (this.mimeType != null) {
            return this.mimeType;
        }

        URLConnection connection = null;
        try {
            connection = this.source.openConnection();
            return connection.getContentType();
        } catch (IOException e) {
            throw new ProcessingException(e);
        } finally {
            URLConnectionUtils.closeQuietly(connection);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ReaderComponent(" + this.source + ")";
    }
}
