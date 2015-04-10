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

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.cocoon.pipeline.util.MurmurHashCodeBuilder;
import org.apache.cocoon.pipeline.util.StringRepresentation;

public class TimestampCacheKey extends AbstractCacheKey {

    private static final long serialVersionUID = 1L;
    private final long timestamp;
    private final URL url;

    public TimestampCacheKey(URL url, long timestamp) {
        super();

        this.url = url;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimestampCacheKey)) {
            return false;
        }

        TimestampCacheKey other = (TimestampCacheKey) obj;
        return this.url.equals(other.url);
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public long getLastModified() {
        return this.getTimestamp();
    }

    @Override
    public int hashCode() {
        return new MurmurHashCodeBuilder().append(this.url.toExternalForm()).append(this.timestamp).toHashCode();
    }

    public boolean isValid(CacheKey cacheKey) {
        if (!this.equals(cacheKey)) {
            return false;
        }

        TimestampCacheKey other = (TimestampCacheKey) cacheKey;
        return this.timestamp == other.timestamp;
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        return StringRepresentation.buildString(this, "url=" + this.url, "timestamp=" + this.timestamp + " ("
                + dateFormat.format(new Date(this.timestamp)) + ")");
    }
}
