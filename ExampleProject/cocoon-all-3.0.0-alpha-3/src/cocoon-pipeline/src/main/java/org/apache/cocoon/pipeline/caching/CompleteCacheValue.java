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
package org.apache.cocoon.pipeline.caching;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.cocoon.pipeline.util.StringRepresentation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CompleteCacheValue extends AbstractCacheValue {

    private static final long serialVersionUID = 1L;
    private final Log logger = LogFactory.getLog(this.getClass());
    private byte[] content;

    public CompleteCacheValue(byte[] content, CacheKey cacheKey) {
        super(cacheKey);

        this.content = content;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.CacheValue#getValue()
     */
    public Object getValue() {
        return this.content;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.CacheValue#setValue()
     */
    public void setValue(Object value) {
        if (value instanceof String) {
            this.content = ((String) value).getBytes();
        } else { // or maybe we should throw exception instead of serializing object ?
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            try {
                ObjectOutputStream objectOut = new ObjectOutputStream(byteOut);
                objectOut.writeObject(value);
                objectOut.flush();
                objectOut.close();
            } catch (IOException e) {
                this.logger.error("Some thing goes wrong during calculating setting value of: " + getCacheKey(), e);
                return;
            }
            this.content = byteOut.toByteArray();
        }
    }

    @Override
    public String toString() {
        return StringRepresentation.buildString(this, "content.length=" + this.content.length, "cacheKey=" + this.getCacheKey());
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.CacheValue#size()
     */
    public double size() {
        return this.content.length;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.apache.cocoon.pipeline.caching.CacheValue#writeTo(java.io.OutputStream)
     */
    public void writeTo(OutputStream outputStream) throws IOException {
        outputStream.write(this.content);
    }

}
